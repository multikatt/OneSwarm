package edu.washington.cs.oneswarm.f2f.servicesharing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.gudy.azureus2.core3.util.DirectByteBuffer;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;

/**
 * Multiplexes a stream of data, and tracks what is in
 * transit on each channel.
 * 
 * @author willscott
 * 
 */
public class MessageStreamMultiplexer {
    private Integer next;
    private final HashMap<Integer, ServiceChannelEndpoint> channels;

    private final HashMap<Integer, SequenceNumber> outstandingMessages;
    private final HashMultimap<Integer, SequenceNumber> channelOutstanding;
    private final static byte ss = 44;

    public MessageStreamMultiplexer() {
        this.channels = new HashMap<Integer, ServiceChannelEndpoint>();
        this.outstandingMessages = new HashMap<Integer, SequenceNumber>();
        this.channelOutstanding = HashMultimap.create();
        next = 0;
    }

    public void addChannel(ServiceChannelEndpoint s) {
        this.channels.put(s.getChannelId()[0], s);
    }

    public void onAck(OSF2FServiceDataMsg message) {
        // Parse acknowledged messages
        DirectByteBuffer payload = message.getPayload();
        HashSet<SequenceNumber> numbers = new HashSet<SequenceNumber>();
        ArrayList<Integer> retransmissions = new ArrayList<Integer>();
        SequenceNumber s = outstandingMessages.get(message.getSequenceNumber());
        if (s != null) {
            numbers.add(s);
        } else {
            retransmissions.add(message.getSequenceNumber());
        }
        while (payload.remaining(ss) > 0) {
            int num = payload.getInt(ss);
            s = outstandingMessages.get(num);
            if (s != null) {
                numbers.add(s);
            } else {
                retransmissions.add(num);
            }
        }

        for (SequenceNumber seq : numbers) {
            if (this.channels.get(seq.getChannel()).forgetMessage(seq)) {
                channelOutstanding.remove(seq.getChannel(), seq);
                outstandingMessages.remove(seq.getNum());
            }
        }
        for (Integer num : retransmissions) {
            System.out.println("Non outstanding packet acked: " + num);
        }
    }

    public SequenceNumber nextMsg(ServiceChannelEndpoint channel) {
        int num = next++;
        int chan = channel.getChannelId()[0];
        SequenceNumber n = new SequenceNumber(num, chan);
        outstandingMessages.put(num, n);
        channelOutstanding.put(chan, n);
        return n;
    }

    public boolean hasOutstanding(ServiceChannelEndpoint channel) {
        return channelOutstanding.containsKey(channel.getChannelId()[0]);
    }

    public Collection<DirectByteBuffer> getOutstanding(final ServiceChannelEndpoint channel) {
        Set<SequenceNumber> outstanding = channelOutstanding.get(channel.getChannelId()[0]);
        return Collections2.transform(outstanding,
                new Function<SequenceNumber, DirectByteBuffer>() {

                    @Override
                    public DirectByteBuffer apply(SequenceNumber s) {
                        return channel.getMessage(s);
                    }
        });
    }

    public void removeChannel(ServiceChannelEndpoint channel) {
        channels.remove(channel.getChannelId()[0]);
        channelOutstanding.removeAll(channel.getChannelId()[0]);
    }
}
