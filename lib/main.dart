import 'package:flutter/material.dart';

import 'api/oauth.dart';
import 'settings.dart' as settings;
import 'specimens/specimens-create.dart';
import 'specimens/specimens-views.dart';

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
        initialRoute: '/specimens',
        routes: {
          '/specimens': (context) => SpecimensViews(title: 'Specimens'),
          '/specimens/create': (context) =>
              SpecimensCreate(title: 'Specimens create')
        });
  }
}
