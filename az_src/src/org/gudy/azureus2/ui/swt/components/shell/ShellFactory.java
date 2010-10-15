package org.gudy.azureus2.ui.swt.components.shell;

/*
 * Created on 17-Mar-2005
 * Created by James Yeh
 * Copyright (C) 2004-2005 Aelitis, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * AELITIS, SAS au capital de 46,603.30 euros
 * 8 Allee Lenotre, La Grille Royale, 78600 Le Mesnil le Roi, France.
 *
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.ui.swt.Utils;
import org.gudy.azureus2.ui.swt.mainwindow.SWTThread;

import com.aelitis.azureus.ui.swt.UIFunctionsManagerSWT;
import com.aelitis.azureus.ui.swt.UIFunctionsSWT;

/**
 * Facilitates the creation of SWT Shells with platform-specific additions.
 * All shells normal to the user should be created from ShellFactory
 * @version 1.0
 * @author James Yeh
 */
public final class ShellFactory
{

	public static Shell createMainShell(int styles) {
		Shell parent = null;

		UIFunctionsSWT uiFunctions = UIFunctionsManagerSWT.getUIFunctionsSWT();

		if (uiFunctions != null) {

			parent = uiFunctions.getMainShell();
		}

		if (parent == null) {

			return createShell(SWTThread.getInstance().getDisplay());
		}

		return (createShell(parent, styles));
	}

	/**
	 * <p>Creates a shell</p>
	 * <p>For platforms that use a unified menu bar, the shell's menu bar is set to the main window's menu bar</p>
	 * @see org.eclipse.swt.widgets.Shell
	 */
	public static Shell createShell(final Display disp, final int styles) {
		return getRegisteredShell(new AEShell(disp, styles));
	}

	/**
	 * <p>Creates a shell</p>
	 * <p>For platforms that use a unified menu bar, the shell's menu bar is set to the main window's menu bar</p>
	 * @see org.eclipse.swt.widgets.Shell
	 */
	public static Shell createShell(final Display disp) {
		return getRegisteredShell(new AEShell(disp));
	}

	/**
	 * <p>Creates a shell</p>
	 * <p>For platforms that use a unified menu bar, the shell's menu bar is set to the main window's menu bar</p>
	 * @see org.eclipse.swt.widgets.Shell
	 */
	public static Shell createShell(final Shell parent, final int styles) {
		if (parent != null && parent.isDisposed())
			return null;

		return getRegisteredShell(new AEShell(parent, styles));
	}

	/**
	 * <p>Creates a shell</p>
	 * <p>For platforms that use a unified menu bar, the shell's menu bar is set to the main window's menu bar</p>
	 * @see org.eclipse.swt.widgets.Shell
	 */
	public static Shell createShell(final Shell parent) {
		return getRegisteredShell(new AEShell(parent));
	}

	/**
	 * <p>Creates a shell</p>
	 * <p>For platforms that use a unified menu bar, the shell's menu bar is set to the main window's menu bar</p>
	 * @see org.eclipse.swt.widgets.Shell
	 */
	public static Shell createShell(final int styles) {
		return getRegisteredShell(new AEShell(styles));
	}

	/**
	 * <p>Gets the registered shell</p>
	 * <p>Registration entails setting its menu bar if platform uses a unified menu bar.</p>
	 * 
	 * <p>On OSX (carbon) the menus for an application is displayed at the top of the screen instead of
	 * on the main window of the application.  This menu is shown whenever the application is activated
	 * or any of its pop up dialogs are activated.  This behavior is very different than that for
	 * Windows and Linux applications because the menu is shown in the main application window for these OS's.</p>
	 * 
	 * <p>To provide the same behavior as native OSX application we must ensure that whenever the application
	 * window or any of its pop up dialog is activate we show the same application menus on the OSX global
	 * menubar.  In the SWT world this means that the same application menu must be created on each shell
	 * that we pop up.</p>
	 * 
	 * <p><b>NOTE:</b> This essentially means that each shell will have its own copy of the main menu so to the users
	 * it would seem like they are looking at the same menu instance.  Moreover, this also means that any
	 * shell-related functions activated through the menu may have to distinguish which shell it is working
	 * with... the main application shell? or a pop up dialog shell?</p>
	 * 
	 * </p>
	 * <p>Also, the shell is added to the shared ShellManager</p>
	 * 
	 * @param toRegister A SWT Shell
	 * @return The SWT Shell
	 */
	private static Shell getRegisteredShell(final Shell toRegister) {

		if (null == toRegister)
			return null;

		if (Constants.isOSX) {
			UIFunctionsSWT uiFunctions = UIFunctionsManagerSWT.getUIFunctionsSWT();
			if (uiFunctions == null)
				throw new IllegalStateException("Main window is not initialized yet");

			uiFunctions.createMainMenu(toRegister);
		}

		ShellManager.sharedManager().addWindow(toRegister);

		return toRegister;
	}

	/**
	 * A shell that provides platform-specific behaviour in some methods in order to better suit the user experience
	 */
	private static class AEShell
		extends Shell
	{
		/**
		 * {@inheritDoc}
		 */
		private AEShell(int styles) {
			super(styles);
		}

		/**
		 * {@inheritDoc}
		 */
		private AEShell(Display display) {
			super(display);
		}

		/**
		 * {@inheritDoc}
		 */
		private AEShell(Display display, int styles) {
			super(display, fixupStyle(styles));
		}

		/**
		 * {@inheritDoc}
		 */
		private AEShell(Shell parent) {
			super(parent);
		}

		/**
		 * {@inheritDoc}
		 */
		private AEShell(Shell parent, int styles) {
			super(parent, fixupStyle(styles));
		}

		static private int fixupStyle(int style) {
			if ((style & (SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL)) > 0
					&& Utils.anyShellHaveStyle(SWT.ON_TOP | SWT.TITLE)) {
				UIFunctionsSWT uiFunctions = UIFunctionsManagerSWT.getUIFunctionsSWT();
				if (uiFunctions != null && uiFunctions.getMainShell() != null) {
					style |= SWT.ON_TOP;
				}
			}
			return style;
		}

		/**
		 * Does nothing
		 */
		protected void checkSubclass() {
		}

		/**
		 * <p>Sets the iconic representation of a SWT window</p>
		 * <p>The icon is often located at the top-left corner of the title bar. This is different from Mac OS X's
		  * document proxy icon.</p>
		 * <p> For Mac OS X, this method does nothing (because the dock's image would be set instead).</p>
		 * @param shell The SWT window
		 * @param imgKey ImageRepository key for the image
		 */
		public void setImage(final Image image) {
			if (!Constants.isOSX)
				super.setImage(image);
		}

		/**
		 * <p>Sets the iconic representation of a SWT window</p>
		 * <p>The icon is often located at the top-left corner of the title bar. This is different from Mac OS X's
		 * document proxy icon.</p>
		 * <p> For Mac OS X, this method does nothing (because the dock's image would be set instead).</p>
		 * @param shell The SWT window
		 * @param images Images
		 */
		public void setImages(final Image[] images) {
			if (!Constants.isOSX)
				super.setImages(images);
		}

		public void open() {
			UIFunctionsSWT uiFunctions = UIFunctionsManagerSWT.getUIFunctionsSWT();
			if (uiFunctions != null) {
				Shell mainShell = uiFunctions.getMainShell();
				if (mainShell != null && mainShell.getMinimized()) {
					uiFunctions.bringToFront();
				}
			}

			super.open();
		}
	}
}