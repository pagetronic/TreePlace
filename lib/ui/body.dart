import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class BodyViews extends StatefulWidget {
  final String title;
  final List<Widget> children;

  BodyViews({Key key, this.children, this.title}) : super(key: key);

  @override
  BodyState createState() => BodyState();
}

class BodyState extends BaseState<BodyViews>
    with SingleTickerProviderStateMixin {
  int initialIndex = 0;

  TabController _controller;

  @override
  void initState() {
    super.initState();
    _controller = new TabController(length: 2, vsync: this);
  }

  @override
  Widget build(BuildContext context) {
    final dynamic args = ModalRoute.of(context).settings.arguments;
    if (args != null && args['index'] != null) {
      initialIndex = args['index'];
    }
    return Scaffold(
        appBar: AppBar(
          title: Text(widget.title),
          actions: <Widget>[connector],
        ),
        body: TabBarView(
          controller: _controller,
          children: widget.children,
        ),
        drawer: Drawer(
          child: ListView(padding: EdgeInsets.zero, children: <Widget>[
            UserAccountsDrawerHeader(
                accountName: Text("User Name"),
                accountEmail: Text("email@gafa.com"),
                onDetailsPressed: () {},
                currentAccountPicture:
                    Image.asset("assets/champagne.png")),
            ListTile(
              selected: initialIndex == 0,
              title: Text('Specimens'),
              onTap: () {
                updateTab(0);
              },
            ),
            ListTile(
              selected: initialIndex == 1,
              title: Text('Forum'),
              onTap: () {
                updateTab(1);
              },
            ),
          ]),
        ));
  }

  void updateTab(int index) {
    setState(() {
      initialIndex = index;
    });
    _controller.animateTo(index);
    Navigator.of(context).pop();
  }
}
