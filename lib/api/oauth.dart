import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uni_links/uni_links.dart';
import 'package:url_launcher/url_launcher.dart';

import '../settings.dart' as settings;

class Oauth {
  static Future<dynamic> getAccessToken() async {
    SharedPreferences storage = await SharedPreferences.getInstance();
    return storage.getStringList("token")[0];
  }

  static Future<dynamic> getRefreshToken() async {
    SharedPreferences storage = await SharedPreferences.getInstance();
    return storage.getStringList("token")[1];
  }

  static Future<String> setToken(dynamic token) async {
    SharedPreferences storage = await SharedPreferences.getInstance();
    await storage.setStringList(
        "token", [token['access_token'], token['refresh_token']]);
  }

  static auth() {
    launch(Uri.https(settings.authDomain, '/auth', {
      'response_type': 'code',
      'client_id': settings.clientId,
      'redirect_uri': settings.redirectUri,
      'scope': settings.scopes
    }).toString());
  }

  static final headers = {
    "User-Agent": settings.userAgent,
    'Content-Type': 'application/json'
  };

  static listenCode() async {
    getUriLinksStream().listen((Uri uri) async {
      if (uri != null) {
        var params = uri.queryParameters;
        String code = params['code'];
        var action = {
          "grant_type": "authorization_code",
          "client_id": settings.clientId,
          "client_secret": settings.clientSecret,
          "code": code
        };

        String jsonBody = json.encode(action);
        final encoding = Encoding.getByName('utf-8');

        http.Response response = await http.post(
          settings.tokenUrl,
          headers: headers,
          body: jsonBody,
          encoding: encoding,
        );

        int statusCode = response.statusCode;
        String responseBody = response.body;
        if (statusCode == 200 && responseBody != null) {
          await setToken(json.decode(responseBody));
        }
      }
    });
  }

  static refreshToken() async {
    var refreshToken = await getRefreshToken();
    var action = {
      "grant_type": "refresh_token",
      "client_id": settings.clientId,
      "client_secret": settings.clientSecret,
      "refresh_token": refreshToken
    };

    String jsonBody = json.encode(action);
    final encoding = Encoding.getByName('utf-8');

    http.Response response = await http.post(
      settings.tokenUrl,
      headers: headers,
      body: jsonBody,
      encoding: encoding,
    );
    int statusCode = response.statusCode;
    String responseBody = response.body;
    if (statusCode == 200 && responseBody != null) {
      await setToken(json.decode(responseBody));
    }
  }

  static void choose() {}
}
