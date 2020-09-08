package android_serialport_api;

import com.example.spirillumin.zzcsoft.utils.Util;
import com.example.spirillumin.zzcsoft.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


/**
 * @author benjaminwan 串口辅助工具类
 */
public abstract class SerialHelper {
private Logger mLogger = LoggerFactory.getLogger(getClass());
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SendThread mSendThread;
	private String sPort = "/dev/s3c2410_serial0";

	private Utils util;
	private int iBaudRate = 9600;
	private boolean _isOpen = false;
	private byte[] _bLoopData = new byte[] { 0x30 };
	private int iDelay = 500;

	// ----------------------------------------------------
	public SerialHelper(String sPort, int iBaudRate) {
		this.sPort = sPort;
		this.iBaudRate = iBaudRate;
		this.util = new Utils();
	}

	public SerialHelper() {
		this("/dev/s3c2410_serial0", 9600);
	}

	public SerialHelper(String sPort) {
		this(sPort, 9600);
	}

	public SerialHelper(String sPort, String sBaudRate) {
		this(sPort, Integer.parseInt(sBaudRate));
	}

	// ----------------------------------------------------
	public void open(){
		try {
			mSerialPort = new SerialPort(new File(sPort), iBaudRate, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			mReadThread = new ReadThread();
			mReadThread.start();
//		mSendThread = new SendThread();
//		mSendThread.setSuspendFlag();
//		mSendThread.start();
			_isOpen = true;
		} catch (Exception e) {
			e.printStackTrace();
			mLogger.debug("open faile port:{}, iBaudRate:{}, Exception:{}", sPort,  iBaudRate, e);
		}
	}

	// ----------------------------------------------------
	public void close() {
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen = false;
	}

	// ----------------------------------------------------
	public void send(byte[] bOutArray) {
		try {
			mLogger.debug("----发送数据：{}", Util.byte2hex(bOutArray));
			mOutputStream.write(bOutArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------
	public void sendHex(String sHex) {
		byte[] bOutArray = util.HexToByteArr(sHex);
		send(bOutArray);
	}

	// ----------------------------------------------------
	public void sendTxt(String sTxt) {
		byte[] bOutArray = sTxt.getBytes();
		send(bOutArray);
	}

	public void sendTxtToAscii(String sTxt) {
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = sTxt.getBytes();
		int bit;

		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
		}
		sendHex(sb.toString().trim());
	}

	// ----------------------------------------------------
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				try {
					if (mInputStream == null)
						return;
					byte[] buffer = new byte[512];
					int size = mInputStream.read(buffer);
					if (size > 0) {
						ComBean ComRecData = new ComBean(sPort, buffer, size);
						mLogger.debug("----接收数据： {}", Util.byte2hex(buffer, 0, size));
						onDataReceived(ComRecData);
					}
					try {
						Thread.sleep(50);// 延时50ms
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (Throwable e) {
					e.printStackTrace();
					mLogger.debug("----ReadThread run {}", e);

//					return;
				}
			}
		}
	}

	// ----------------------------------------------------
	private class SendThread extends Thread {
		public volatile boolean suspendFlag = true;// 控制线程的执行

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				while (suspendFlag) {
					try {
						synchronized (this) {
							wait();
						}
					} catch (Exception e) {
						e.printStackTrace();
						mLogger.debug("----SendThread run {}", e);
					}
				}

				try {
					byte[] sendDataByte = getbLoopData();
					mLogger.debug("----发送数据：{}", Util.byte2hex(sendDataByte));
					send(sendDataByte);
					Thread.sleep(iDelay);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// 线程暂停
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}

		// 唤醒线程
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
	}

	// ----------------------------------------------------
	public int getBaudRate() {
		return iBaudRate;
	}

	public boolean setBaudRate(int iBaud) {
		if (_isOpen) {
			return false;
		} else {
			iBaudRate = iBaud;
			return true;
		}
	}

	public boolean setBaudRate(String sBaud) {
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}

	// ----------------------------------------------------
	public String getPort() {
		return sPort;
	}

	public boolean setPort(String sPort) {
		if (_isOpen) {
			return false;
		} else {
			this.sPort = sPort;
			return true;
		}
	}

	// ----------------------------------------------------
	public boolean isOpen() {
		return _isOpen;
	}

	// ----------------------------------------------------
	public byte[] getbLoopData() {
		return _bLoopData;
	}

	// ----------------------------------------------------
	public void setbLoopData(byte[] bLoopData) {
		this._bLoopData = bLoopData;
	}

	// ----------------------------------------------------
	public void setTxtLoopData(String sTxt) {
		this._bLoopData = sTxt.getBytes();
	}

	// ----------------------------------------------------
	public void setHexLoopData(String sHex) {
		this._bLoopData = util.HexToByteArr(sHex);
	}

	// ----------------------------------------------------
	public int getiDelay() {
		return iDelay;
	}

	// ----------------------------------------------------
	public void setiDelay(int iDelay) {
		this.iDelay = iDelay;
	}

	// ----------------------------------------------------
	public void startSend() {
		if (mSendThread != null) {
			mSendThread.setResume();
		}
	}

	// ----------------------------------------------------
	public void stopSend() {
		if (mSendThread != null) {
			mSendThread.setSuspendFlag();
		}
	}

	// ----------------------------------------------------
	protected abstract void onDataReceived(ComBean ComRecData);
}