
package com.druidkuma.vk;

import java.io.IOException;

import com.jfixby.scarabei.api.desktop.DesktopSetup;
import com.jfixby.scarabei.api.log.L;

/** Created by Iurii Miedviediev
 *
 * Simple example of using VK access token getter
 *
 * @author DruidKuma
 * @version 1.0.0
 * @since 12/23/15 */
public class Main {
	public static void main (final String[] args) throws IOException {
		DesktopSetup.deploy();
		L.d(String.format("Retrieving Access Token for application (%s) with scopes (%s)", Properties.APP_ID, Properties.SCOPE));
		L.d(String.format("Retrieved Token Value: %s",
			VKTokenGetter.getAccessToken(Properties.APP_ID, Properties.SCOPE, Properties.USER_LOGIN, Properties.USER_PASSWORD)));
	}
}
