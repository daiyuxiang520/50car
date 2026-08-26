package com.fiftycar.app.di

import com.fiftycar.app.data.CarApi
import com.fiftycar.app.data.MockCarApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * 车控 API 绑定:
     * 默认 MockCarApi(演示,离线可跑通);
     * ★ 对接真实车辆时,实现你自己的 CarApi 并在此换绑 ——
     *   请使用你自己获得授权的凭据,本工程不提供也不允许任何第三方私有密钥。
     */
    @Binds
    @Singleton
    abstract fun bindCarApi(impl: MockCarApi): CarApi
}
