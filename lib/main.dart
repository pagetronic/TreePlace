import 'package:flutter/material.dart';

import 'api/oauth.dart';
import 'forum/forum.dart';
import 'forum/thread.dart';
import 'settings.dart' as settings;
import 'specimens/create.dart';
import 'specimens/specimen.dart';
import 'specimens/specimens.dart';
import 'ui/body.dart';
import 'ui/images.dart';

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
        initialRoute: '/initial',
        routes: {
          '/initial': (context) => BodyViews(
                title: "Agroneo",
                children: <Widget>[
                  SpecimensViews(title: 'Specimens'),
                  ForumsViews(title: 'Forum')
                ],
              ),
          '/specimens/create': (context) =>
              SpecimensCreate(title: 'Specimens create'),
          '/specimens/view': (context) => SpecimenView(title: 'Specimen view'),
          '/thread': (context) => ThreadView(),
          '/images': (context) => ImageView()
        });
  }
}
