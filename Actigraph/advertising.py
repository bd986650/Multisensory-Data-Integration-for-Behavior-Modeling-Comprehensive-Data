from __future__ import print_function
import dbus
import dbus.exceptions
import dbus.mainloop.glib
import dbus.service
import functools

import exceptions
import adapters


BLUEZ_SERVICE_NAME = 'org.bluez'
LE_ADVERTISING_MANAGER_IFACE = 'org.bluez.LEAdvertisingManager1'
DBUS_OM_IFACE = 'org.freedesktop.DBus.ObjectManager'
DBUS_PROP_IFACE = 'org.freedesktop.DBus.Properties'

LE_ADVERTISEMENT_IFACE = 'org.bluez.LEAdvertisement1'


class Advertisement(dbus.service.Object):
    PATH_BASE = '/org/bluez/example/advertisement'

    def __init__(self, bus, index, advertising_type):
        self.path = self.PATH_BASE + str(index)
        self.bus = bus
        self.ad_type = advertising_type
        self.service_uuids = None
        self.manufacturer_data = None
        self.solicit_uuids = None
        self.service_data = None
        self.include_tx_power = None
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        properties = dict()
        properties['Type'] = self.ad_type
        if self.service_uuids is not None:
            properties['ServiceUUIDs'] = dbus.Array(self.service_uuids,
                                                    signature='s')
        if self.solicit_uuids is not None:
            properties['SolicitUUIDs'] = dbus.Array(self.solicit_uuids,
                                                    signature='s')
        if self.manufacturer_data is not None:
            properties['ManufacturerData'] = dbus.Dictionary(
                self.manufacturer_data, signature='qv')
        if self.service_data is not None:
            properties['ServiceData'] = dbus.Dictionary(self.service_data,
                                                        signature='sv')
        if self.include_tx_power is not None:
            properties['IncludeTxPower'] = dbus.Boolean(self.include_tx_power)
        return {LE_ADVERTISEMENT_IFACE: properties}

    def get_path(self):
        return dbus.ObjectPath(self.path)

    def add_service_uuid(self, uuid):
        if not self.service_uuids:
            self.service_uuids = []
        self.service_uuids.append(uuid)

    def add_solicit_uuid(self, uuid):
        if not self.solicit_uuids:
            self.solicit_uuids = []
        self.solicit_uuids.append(uuid)

    def add_manufacturer_data(self, manuf_code, data):
        if not self.manufacturer_data:
            self.manufacturer_data = dbus.Dictionary({}, signature='qv')
        self.manufacturer_data[manuf_code] = dbus.Array(data, signature='y')

    def add_service_data(self, uuid, data):
        if not self.service_data:
            self.service_data = dbus.Dictionary({}, signature='sv')
        self.service_data[uuid] = dbus.Array(data, signature='y')

    @dbus.service.method(DBUS_PROP_IFACE,
                         in_signature='s',
                         out_signature='a{sv}')
    def GetAll(self, interface):
        print('GetAll')
        if interface != LE_ADVERTISEMENT_IFACE:
            raise exceptions.InvalidArgsException()
        print('returning props')
        return self.get_properties()[LE_ADVERTISEMENT_IFACE]

    @dbus.service.method(LE_ADVERTISEMENT_IFACE,
                         in_signature='',
                         out_signature='')
    def Release(self):
        print('%s: Released!' % self.path)


class TestAdvertisement(dbus.service.Object):
    """
    org.bluez.LEAdvertisement1 interface implementation
    """
    def __init__(self, bus, index):
        self.path = '/org/bluez/example/advertisement' + str(index)
        self.bus = bus
        self.service_uuids = ["8ec91400-f315-4f60-9fb8-838830daea50"]
        self.manufacturer_data = {0x0401: [0x8f, 0x10, 0x00, 0x00, 0x05, 0x01]}
        self.include_tx_power = True
        self.local_name = "Dock Station"
        dbus.service.Object.__init__(self, bus, self.path)

    def get_properties(self):
        properties = dict()
        properties['Type'] = 'broadcast'
        properties['ServiceUUIDs'] = dbus.Array(self.service_uuids,
                                              signature='s')
        properties['ManufacturerData'] = dbus.Dictionary(self.manufacturer_data,
                                                       signature='qv')
        properties['IncludeTxPower'] = dbus.Boolean(self.include_tx_power)
        properties['LocalName'] = self.local_name
        return {LE_ADVERTISEMENT_IFACE: properties}

    def get_path(self):
        return dbus.ObjectPath(self.path)

    @dbus.service.method(DBUS_OM_IFACE, out_signature='a{sv}')
    def GetAll(self):
        return self.get_properties()

    @dbus.service.method(LE_ADVERTISEMENT_IFACE,
                        in_signature='',
                        out_signature='')
    def Release(self):
        print('%s: Released!' % self.path)


def register_ad_cb():
    print('Advertisement registered successfully')
    print('Sending advertising packets with UUID: 8ec91400-f315-4f60-9fb8-838830daea50')
    print('Manufacturer ID: 0x0401')
    print('Manufacturer Data: 8f1000000501')
    print('Server is now running and advertising...')
    print('Press Ctrl+C to stop')


def register_ad_error_cb(mainloop, error):
    print('Failed to register advertisement: ' + str(error))
    print('Error details:', error)
    print('Stack trace:', error.get_dbus_message())
    mainloop.quit()


def advertising_main(mainloop, bus, adapter_name):
    try:
        print('Starting advertising_main...')
        adapter = adapters.find_adapter(bus, LE_ADVERTISING_MANAGER_IFACE, adapter_name)
        print('Found adapter: %s' % (adapter,))
        if not adapter:
            raise Exception('LEAdvertisingManager1 interface not found')

        adapter_props = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, adapter),
                                     "org.freedesktop.DBus.Properties")

        print('Setting adapter properties...')
        try:
            adapter_props.Set("org.bluez.Adapter1", "Powered", dbus.Boolean(1))
            print('Adapter powered on')
            adapter_props.Set("org.bluez.Adapter1", "Discoverable", dbus.Boolean(1))
            print('Adapter discoverable')
            adapter_props.Set("org.bluez.Adapter1", "DiscoverableTimeout", dbus.UInt32(0))
            adapter_props.Set("org.bluez.Adapter1", "PairableTimeout", dbus.UInt32(0))
            print('Adapter timeouts set')
        except Exception as e:
            print('Error setting adapter properties:', str(e))
            raise

        print('Getting advertising manager...')
        ad_manager = dbus.Interface(bus.get_object(BLUEZ_SERVICE_NAME, adapter),
                                  LE_ADVERTISING_MANAGER_IFACE)
        print('Got advertising manager')

        print('Creating advertisement...')
        test_advertisement = TestAdvertisement(bus, 0)
        print('Advertisement created with UUID: 8ec91400-f315-4f60-9fb8-838830daea50')
        print('Manufacturer ID: 0x0401')
        print('Manufacturer Data: 8f1000000501')
        print('Local Name: Dock Station')
        print('Advertising on channels 0-19')

        print('Registering advertisement...')
        try:
            ad_manager.RegisterAdvertisement(test_advertisement.get_path(), {},
                                           reply_handler=register_ad_cb,
                                           error_handler=functools.partial(register_ad_error_cb, mainloop))
            print('Advertisement registration request sent')
        except Exception as e:
            print('Error registering advertisement:', str(e))
            raise
    except Exception as e:
        print('Error in advertising_main:', str(e))
        print('Stack trace:', e.__traceback__)
        mainloop.quit()

