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

  //TODO to cache
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
          icon: Image.network(rez['logo'] + '@64x64.png'),
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
