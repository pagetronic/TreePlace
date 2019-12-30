import 'package:agroneo_treeplace/api/api.dart';
import 'package:agroneo_treeplace/api/oauth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

abstract class BaseState<T extends StatefulWidget> extends State<T> {
  IconButton connector = IconButton(
    icon: const Icon(Icons.account_circle),
    tooltip: 'Connection',
    onPressed: () {
      Oauth.auth();
    },
  );

  void updateConnection() {
    setState(() {
      connector = IconButton(
        icon: const CircularProgressIndicator(),
        tooltip: 'loading..',
        onPressed: () {
          Oauth.choose();
        },
      );
    });
    ApiRequest.get('/profile', success: ((rez) {
      setState(() {
        connector = IconButton(
          icon: new Image.network(rez['logo'] + '@32'),
          tooltip: rez['name'],
          onPressed: () {
            Oauth.choose();
          },
        );
      });
    }), error: ((code, rez) {
      setState(() {
        connector = IconButton(
          icon: const Icon(Icons.account_circle),
          tooltip: 'Connection',
          onPressed: () {
            Oauth.auth();
          },
        );
      });
    }));
  }

  @override
  void initState() {
    super.initState();
    updateConnection();
  }
}
