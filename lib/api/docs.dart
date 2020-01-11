import 'package:flutter/cupertino.dart';
import 'package:web_socket_channel/io.dart';

import '../settings.dart' as settings;

class Documents {
  static upload(Function(dynamic) success) {

    //TODO choose file and upload
    /*
    final channel = IOWebSocketChannel.connect(settings.upSocket);
    StreamBuilder(
      stream: channel.stream,
      builder: (context, snapshot) {
        return Text(snapshot.hasData ? '${snapshot.data}' : '');
      },
    );

    channel.sink.add('Hello!');
    channel.sink.close();

     */
  }
}
