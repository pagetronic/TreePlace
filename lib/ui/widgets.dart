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
  void updateConnection(bool state) async {
    IconButton button = IconButton(
      icon: const Icon(Icons.account_circle),
      tooltip: 'Connection',
      onPressed: () {
        Oauth.auth();
      },
    );
    if (state) {
      setState(() {
        connector = button;
      });
    } else {
      connector = button;
    }
    ApiRequest.get('/profile', success: ((rez) {
      if (rez['id'] == null) {
        button = IconButton(
          icon: const Icon(Icons.account_circle),
          tooltip: 'Connection',
          onPressed: () {
            Oauth.auth();
          },
        );
      } else {
        button = IconButton(
          icon: Image.network(rez['logo'] + '@64x64.png'),
          tooltip: rez['name'],
          onPressed: () {
            Oauth.choose();
          },
        );
      }
      if (state) {
        setState(() {
          connector = button;
        });
      } else {
        connector = button;
      }
    }), error: ((code, rez) {
      button = IconButton(
        icon: const Icon(Icons.account_circle),
        tooltip: 'Connection',
        onPressed: () {
          Oauth.auth();
        },
      );
      if (state) {
        setState(() {
          connector = button;
        });
      } else {
        connector = button;
      }
    }));
  }

  @override
  void initState() {
    super.initState();
    updateConnection(mounted);
  }
}
