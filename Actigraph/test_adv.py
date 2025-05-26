#!/usr/bin/python3

import dbus
import dbus.exceptions
import dbus.mainloop.glib
import dbus.service

import array
from gi.repository import GObject
import sys

from random import randint

BLUEZ_SERVICE_NAME = 'org.bluez'
LE_ADVERTISING_MANAGER_IFACE = 'org.bluez.LEAdvertisingManager1'
DBUS_OM_IFACE = 'org.freedesktop.DBus.ObjectManager'
DBUS_PROP_IFACE = 'org.freedesktop.DBus.Properties'

LE_ADVERTISEMENT_IFACE = 'org.bluez.LEAdvertisement1'

class Advertisement(dbus.service.Object):
    def __init__(self, bus, index):
        self.path = '/org/bluez/example/advertisement' + str(index)
        self.bus = bus
        self.service_uuids = ["8ec91400-f315-4f60-9fb8-838830daea50"]
        self.manufacturer_data = {0x0401: [0x8f, 0x10, 0x00, 0x00, 0x05, 0x01]}
        self.local_name = "Dock Station"
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        return {
            LE_ADVERTISEMENT_IFACE: {
                'Type': 'broadcast',
                'ServiceUUIDs': dbus.Array(self.service_uuids, signature='s'),
                'ManufacturerData': dbus.Dictionary(self.manufacturer_data, signature='qv'),
                'LocalName': self.local_name
            }
        }

    def get_path(self):
        return dbus.ObjectPath(self.path)

    @dbus.service.method(DBUS_OM_IFACE, out_signature='a{sv}')
    def GetAll(self):
        return self.get_properties()

    @dbus.service.method(LE_ADVERTISEMENT_IFACE, in_signature='', out_signature='')
    def Release(self):
        print('%s: Released!' % self.path)

def find_adapter(bus):
    remote_om = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, '/'),
                             DBUS_OM_IFACE)
    objects = remote_om.GetManagedObjects()

    for o, props in objects.items():
        if LE_ADVERTISING_MANAGER_IFACE in props:
            return o
    return None

def main():
    dbus.mainloop.glib.DBusGMainLoop(set_as_default=True)
    bus = dbus.SystemBus()

    # Find the adapter
    adapter_path = find_adapter(bus)
    if not adapter_path:
        print('LEAdvertisingManager1 interface not found')
        return

    # Get the BlueZ interface
    adapter = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, adapter_path),
                           LE_ADVERTISING_MANAGER_IFACE)

    # Create advertisement
    advertisement = Advertisement(bus, 0)

    # Register advertisement
    adapter.RegisterAdvertisement(advertisement.get_path(),
                                {},
                                reply_handler=register_ad_cb,
                                error_handler=register_ad_error_cb)

    # Start main loop
    mainloop = GObject.MainLoop()
    mainloop.run()

def register_ad_cb():
    print('Advertisement registered successfully')
    print('Sending advertising packets with UUID: 8ec91400-f315-4f60-9fb8-838830daea50')
    print('Manufacturer ID: 0x0401')
    print('Manufacturer Data: 8f 10 00 00 05 01')
    print('Local Name: Dock Station')
    print('Server is now running and advertising...')
    print('Press Ctrl+C to stop')

def register_ad_error_cb(error):
    print('Failed to register advertisement: ' + str(error))
    sys.exit(1)

if __name__ == '__main__':
    main()