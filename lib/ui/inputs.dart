import 'package:flutter/material.dart';

// Define a custom Form widget.
class TextObjects extends StatefulWidget {
  TextObjectsState state;
  TextEditingController controller = TextEditingController();

  @override
  TextObjectsState createState() {
    state = TextObjectsState();
    return state;
  }

  String text() {
    return controller.text;
  }
}

class TextObjectsState extends State<TextObjects> {
  @override
  Widget build(BuildContext context) {
    TextField area = TextField(
      controller: widget.controller,
      minLines: 3,
      maxLines: 10000,
      toolbarOptions: ToolbarOptions(copy: true, cut: true, paste: true),
    );

    var obj = Column(children: <Widget>[
      area,
      SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: <Widget>[
              OutlineButton(
                padding: EdgeInsets.all(0.0),
                child: Text('link',
                    style: TextStyle(decoration: TextDecoration.underline)),
                onPressed: () {
                  insert("[url]", "[/url]");
                },
              ),
              OutlineButton(
                padding: EdgeInsets.all(0.0),
                child: Text('doc'),
                onPressed: null,
              ),
              OutlineButton(
                padding: EdgeInsets.all(0.0),
                child:
                    Text('bold', style: TextStyle(fontWeight: FontWeight.bold)),
                onPressed: () {
                  insert("[bold]", "[/bold]");
                },
              ),
              OutlineButton(
                padding: EdgeInsets.all(0.0),
                child: Text('italic',
                    style: TextStyle(fontStyle: FontStyle.italic)),
                onPressed: () {
                  insert("[italic]", "[/italic]");
                },
              ),
              OutlineButton(
                padding: EdgeInsets.all(0.0),
                child: Text('quote'),
                onPressed: () {
                  insert("[quote]", "[/quote]");
                },
              )
            ],
          ))
    ]);
    return obj;
  }

  @override
  void dispose() {
    widget.controller.dispose();
    super.dispose();
  }

  void insert(String before, String after) {
    var selection = widget.controller.selection;
    String old = widget.controller.text;
    String newText = old.substring(0, selection.start) +
        before +
        old.substring(selection.start, selection.end) +
        after +
        old.substring(selection.end, old.length);
    widget.controller.text = newText;
  }
}
