package com.agroneo.treeplace.auth;

import android.accounts.*;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;

public class AuthService extends Service {
	@Override
	public IBinder onBind(Intent intent) {

		AgroneoAuthenticator authenticator = new AgroneoAuthenticator(this);
		return authenticator.getIBinder();
	}

	private static class AgroneoAuthenticator extends AbstractAccountAuthenticator {

		private final Context mContext;

		public AgroneoAuthenticator(Context context) {
			super(context);
			this.mContext = context;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
			return null;
		}

		@Override
		public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
			Intent intent = new Intent(mContext, AuthActivity.class);
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
			Bundle bundle = new Bundle();
			bundle.putParcelable(AccountManager.KEY_INTENT, intent);
			return bundle;
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			return null;
		}


		@Override
		public String getAuthTokenLabel(String authTokenType) {
			if (authTokenType.equals("refresh")) {
				return "refresh_token";
			} else if (authTokenType.equals("access")) {
				return "access_token";
			}
			return "";
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
			final Bundle result = new Bundle();
			result.putBoolean(KEY_BOOLEAN_RESULT, false);
			return result;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
			return null;
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
			return null;
		}
	}

}
