package com.hhmusic.uitl;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;


public class PermissionHelper {

	private static final String TAG = "PermissionHelper";


	private final static int READ_PHONE_STATE_CODE = 101;

	private final static int WRITE_EXTERNAL_STORAGE_CODE = 102;

	private final static int REQUEST_OPEN_APPLICATION_SETTINGS_CODE = 12345;


	private PermissionModel[] mPermissionModels = new PermissionModel[] {
			new PermissionModel("电话", Manifest.permission.READ_PHONE_STATE, "我们需要读取手机信息的权限来标识您的身份", READ_PHONE_STATE_CODE),
			new PermissionModel("存储空间", Manifest.permission.WRITE_EXTERNAL_STORAGE, "我们需要您允许我们读写你的存储卡，以方便我们临时保存一些数据",
					WRITE_EXTERNAL_STORAGE_CODE)
	};

	private Activity mActivity;

	private OnApplyPermissionListener mOnApplyPermissionListener;

	public PermissionHelper(Activity activity) {
		mActivity = activity;
	}

	public void setOnApplyPermissionListener(OnApplyPermissionListener onApplyPermissionListener) {
		mOnApplyPermissionListener = onApplyPermissionListener;
	}


	public void applyPermissions() {
		try {
			for (final PermissionModel model : mPermissionModels) {
				if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity, model.permission)) {
					ActivityCompat.requestPermissions(mActivity, new String[] { model.permission }, model.requestCode);
					return;
				}
			}
			if (mOnApplyPermissionListener != null) {
				mOnApplyPermissionListener.onAfterApplyAllPermission();
			}
		} catch (Throwable e) {
			Log.e(TAG, "", e);
		}
	}


	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
		case READ_PHONE_STATE_CODE:
		case WRITE_EXTERNAL_STORAGE_CODE:

			if (PackageManager.PERMISSION_GRANTED != grantResults[0]) {


				if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[0])) {
					AlertDialog.Builder builder =
							new AlertDialog.Builder(mActivity).setTitle("权限申请").setMessage(findPermissionExplain(permissions[0]))
									.setPositiveButton("确定", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											applyPermissions();
										}
									});
					builder.setCancelable(false);
					builder.show();
				}

				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity).setTitle("权限申请")
							.setMessage("请在打开的窗口的权限中开启" + findPermissionName(permissions[0]) + "权限，以正常使用本应用")
							.setPositiveButton("去设置", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									openApplicationSettings(REQUEST_OPEN_APPLICATION_SETTINGS_CODE);
								}
							}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mActivity.finish();
								}
							});
					builder.setCancelable(false);
					builder.show();
				}
				return;
			}


			if (isAllRequestedPermissionGranted()) {
				if (mOnApplyPermissionListener != null) {
					mOnApplyPermissionListener.onAfterApplyAllPermission();
				}
			} else {
				applyPermissions();
			}
			break;
		}
	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_OPEN_APPLICATION_SETTINGS_CODE:
			if (isAllRequestedPermissionGranted()) {
				if (mOnApplyPermissionListener != null) {
					mOnApplyPermissionListener.onAfterApplyAllPermission();
				}
			} else {
				mActivity.finish();
			}
			break;
		}
	}


	public boolean isAllRequestedPermissionGranted() {
		for (PermissionModel model : mPermissionModels) {
			if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(mActivity, model.permission)) {
				return false;
			}
		}
		return true;
	}


	private boolean openApplicationSettings(int requestCode) {
		try {
			Intent intent =
					new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + mActivity.getPackageName()));
			intent.addCategory(Intent.CATEGORY_DEFAULT);


			mActivity.startActivityForResult(intent, requestCode);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, "", e);
		}
		return false;
	}


	private String findPermissionExplain(String permission) {
		if (mPermissionModels != null) {
			for (PermissionModel model : mPermissionModels) {
				if (model != null && model.permission != null && model.permission.equals(permission)) {
					return model.explain;
				}
			}
		}
		return null;
	}


	private String findPermissionName(String permission) {
		if (mPermissionModels != null) {
			for (PermissionModel model : mPermissionModels) {
				if (model != null && model.permission != null && model.permission.equals(permission)) {
					return model.name;
				}
			}
		}
		return null;
	}

	private static class PermissionModel {


		public String name;


		public String permission;


		public String explain;


		public int requestCode;

		public PermissionModel(String name, String permission, String explain, int requestCode) {
			this.name = name;
			this.permission = permission;
			this.explain = explain;
			this.requestCode = requestCode;
		}
	}


	public interface OnApplyPermissionListener {


		void onAfterApplyAllPermission();
	}
	
}
