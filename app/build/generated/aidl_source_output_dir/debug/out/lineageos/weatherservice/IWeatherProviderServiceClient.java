/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package lineageos.weatherservice;
public interface IWeatherProviderServiceClient extends android.os.IInterface
{
  /** Default implementation for IWeatherProviderServiceClient. */
  public static class Default implements lineageos.weatherservice.IWeatherProviderServiceClient
  {
    @Override public void setServiceRequestState(lineageos.weather.RequestInfo requestInfo, lineageos.weatherservice.ServiceRequestResult result, int state) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements lineageos.weatherservice.IWeatherProviderServiceClient
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an lineageos.weatherservice.IWeatherProviderServiceClient interface,
     * generating a proxy if needed.
     */
    public static lineageos.weatherservice.IWeatherProviderServiceClient asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof lineageos.weatherservice.IWeatherProviderServiceClient))) {
        return ((lineageos.weatherservice.IWeatherProviderServiceClient)iin);
      }
      return new lineageos.weatherservice.IWeatherProviderServiceClient.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_setServiceRequestState:
        {
          lineageos.weather.RequestInfo _arg0;
          _arg0 = _Parcel.readTypedObject(data, lineageos.weather.RequestInfo.CREATOR);
          lineageos.weatherservice.ServiceRequestResult _arg1;
          _arg1 = _Parcel.readTypedObject(data, lineageos.weatherservice.ServiceRequestResult.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          this.setServiceRequestState(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements lineageos.weatherservice.IWeatherProviderServiceClient
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void setServiceRequestState(lineageos.weather.RequestInfo requestInfo, lineageos.weatherservice.ServiceRequestResult result, int state) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, requestInfo, 0);
          _Parcel.writeTypedObject(_data, result, 0);
          _data.writeInt(state);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setServiceRequestState, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_setServiceRequestState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public static final java.lang.String DESCRIPTOR = "lineageos.weatherservice.IWeatherProviderServiceClient";
  public void setServiceRequestState(lineageos.weather.RequestInfo requestInfo, lineageos.weatherservice.ServiceRequestResult result, int state) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
