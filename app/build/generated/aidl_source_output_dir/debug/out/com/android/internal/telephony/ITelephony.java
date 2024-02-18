/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.android.internal.telephony;
public interface ITelephony extends android.os.IInterface
{
  /** Default implementation for ITelephony. */
  public static class Default implements com.android.internal.telephony.ITelephony
  {
    @Override public boolean endCall() throws android.os.RemoteException
    {
      return false;
    }
    @Override public void answerRingingCall() throws android.os.RemoteException
    {
    }
    @Override public void silenceRinger() throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.internal.telephony.ITelephony
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.internal.telephony.ITelephony interface,
     * generating a proxy if needed.
     */
    public static com.android.internal.telephony.ITelephony asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.internal.telephony.ITelephony))) {
        return ((com.android.internal.telephony.ITelephony)iin);
      }
      return new com.android.internal.telephony.ITelephony.Stub.Proxy(obj);
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
        case TRANSACTION_endCall:
        {
          boolean _result = this.endCall();
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_answerRingingCall:
        {
          this.answerRingingCall();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_silenceRinger:
        {
          this.silenceRinger();
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
    private static class Proxy implements com.android.internal.telephony.ITelephony
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
      @Override public boolean endCall() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_endCall, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void answerRingingCall() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_answerRingingCall, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void silenceRinger() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_silenceRinger, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    public static final java.lang.String DESCRIPTOR = "com.android.internal.telephony.ITelephony";
    static final int TRANSACTION_endCall = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_answerRingingCall = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_silenceRinger = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  public boolean endCall() throws android.os.RemoteException;
  public void answerRingingCall() throws android.os.RemoteException;
  public void silenceRinger() throws android.os.RemoteException;
}
