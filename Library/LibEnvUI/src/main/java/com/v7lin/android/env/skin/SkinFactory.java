package com.v7lin.android.env.skin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

/**
 * @author v7lin E-mail:v7lin@qq.com
 */
public class SkinFactory {

	private static final SkinCreator SKIN_CREATOR = new SkinCreator() {

		@Override
		public SkinFamily createFrom(Context context, Resources originalRes, String skinPath) {
			SkinFamily family = null;
			try {
				PackageManager manager = context.getPackageManager();
				PackageInfo info = manager.getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES);
				String skinPkg = info.packageName;

				Class<?> clazz = AssetManager.class;
				AssetManager skinAsset = (AssetManager) clazz.newInstance();
				Method method = clazz.getDeclaredMethod("addAssetPath", String.class);
				method.invoke(skinAsset, skinPath);
				// 由于这里的资源都是非系统级别的，不会被缓存到Resources的static成员常量里
				// 即Resources的startPreloading和finishPreloading已在ZygoteInit中被调用完毕，一些启动所需的系统资源被缓存到Resources的static成员常量里
				// 所以可以不用EnvThirdResources
				Resources skinRes = new Resources(skinAsset, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());

				family = new SkinFamily(skinPath, skinPkg, skinRes, originalRes);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return family;
		}
	};

	public static boolean isValid(SkinFamily family) {
		boolean isValid = false;
		try {
			if (family != null && family.getSkinRes() != null && family.getSkinRes().getAssets() != null) {
				AssetManager assets = family.getSkinRes().getAssets();
				Class<?> clazz = assets.getClass();
				Method method = clazz.getDeclaredMethod("isUpToDate");
				Object object = method.invoke(assets);
				isValid = Boolean.valueOf(String.valueOf(object)).booleanValue();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return isValid;
	}

	public static SkinFamily makeSkin(Context context, Resources originalRes, String skinPath) {
		SkinFamily family = null;
		if (!TextUtils.isEmpty(skinPath)) {
			family = SKIN_CREATOR.createFrom(context, originalRes, skinPath);
		}
		return family;
	}
}
