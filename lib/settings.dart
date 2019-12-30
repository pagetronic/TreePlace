import 'dart:ui';

final clientId = 'QULU2HS2DY2LK663AONAMELHYQC3MG6JUW9QAF6NV1M49YQ7QM0P';
final clientSecret = '11I5G3W36JWZ940IS4R78ZCAVH8SIIQJJ0Q4P61EWAC64MEJ1IT4';
final redirectUri = 'agroneo-auth://';
final scopes = 'email,gaia';
final tokenUrl = 'https://api.agroneo.com/token';
final apiUrl = 'https://api.agroneo.com';
final authDomain = () {
  var domain = 'en.agroneo.com';
  String lng = Locale.cachedLocaleString;
  if (lng.startsWith('fr')) {
    domain = 'fr.agroneo.com';
  } else if (lng.startsWith('pt')) {
    domain = 'pt.agroneo.com';
  } else if (lng.startsWith('es')) {
    domain = 'es.agroneo.com';
  }
  return domain;
}();
final userAgent = "Agroneo TreePlace Android V3";

Color primaryColor = Color(0xff31771d);
Color secondaryColor = Color(0xffa96923);
