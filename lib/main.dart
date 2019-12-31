import 'package:flutter/material.dart';

import 'api/oauth.dart';
import 'forum/forums-view.dart';
import 'settings.dart' as settings;
import 'specimens/specimens-create.dart';
import 'specimens/specimens-views.dart';
import 'ui/body.dart';

void main() {
  runApp(TreePlaceApp());
  Oauth.listenCode();
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
          '/specimens': (context) => BodyViews(
                title: "Agroneo",
                children: <Widget>[
                  SpecimensViews(title: 'Specimens'),
                  ForumsViews(title: 'Forum')
                ],
              ),
          '/specimens/create': (context) =>
              SpecimensCreate(title: 'Specimens create')
        });
  }
}
