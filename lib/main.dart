import 'package:flutter/material.dart';

import 'api/oauth.dart';
import 'settings.dart' as settings;
import 'specimens/specimens.dart';

void main() {
  Oauth.listenCode();
  runApp(TreePlaceApp());
}

class TreePlaceApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Agroneo TreePlace',
        theme: ThemeData(
          primaryColor: settings.primaryColor,
        ),
        home: SpecimensPage(title: 'Specimens'));
  }
}
