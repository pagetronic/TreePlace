import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class SpecimensMap extends StatefulWidget {
  SpecimensMap({Key key}) : super(key: key);

  @override
  State<SpecimensMap> createState() => SpecimensMapState();
}

class SpecimensMapState extends State<SpecimensMap> {
  Completer<GoogleMapController> _controller = Completer();

  Set<Marker> markers = Set<Marker>();

  @override
  Widget build(BuildContext context) {
    final dynamic json = ModalRoute.of(context).settings.arguments;

    List<dynamic> coordinates = json['location']['coordinates'];

    CameraPosition kspecimen = CameraPosition(
      target: LatLng(coordinates[1], coordinates[0]),
      zoom: 14.4746,
    );

    return new Scaffold(
      body: GoogleMap(
        markers: markers,
        mapType: MapType.hybrid,
        initialCameraPosition: kspecimen,
        onMapCreated: (GoogleMapController controller) {
          _controller.complete(controller);
          setMarker(LatLng(coordinates[1], coordinates[0]), json);
        },
      ),
    );
  }

  void setMarker(LatLng latLng, dynamic json) async {
    BitmapDescriptor icon =
        await BitmapDescriptor.fromAssetImage(createLocalImageConfiguration(context, size: Size(30, 30)), 'assets/marker.png');
    Marker marker = Marker(
      icon: icon,
      markerId: new MarkerId("xxxxx"),
      position: latLng,
      onTap: () {
        Navigator.pushNamed(context, '/specimens/view',
            arguments: {'id': json['id'], 'title': json['title']});
      },
    );
    setState(() {
      markers.add(marker);
    });
  }
}
