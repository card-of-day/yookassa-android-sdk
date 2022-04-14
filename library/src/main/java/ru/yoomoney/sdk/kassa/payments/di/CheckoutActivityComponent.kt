/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yoomoney.sdk.kassa.payments.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.yoomoney.sdk.kassa.payments.payment.PaymentMethodId
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.TestParameters
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.UiParameters
import ru.yoomoney.sdk.kassa.payments.config.ConfigModule
import ru.yoomoney.sdk.kassa.payments.ui.CheckoutActivity
import ru.yoomoney.sdk.kassa.payments.ui.MainDialogFragment
import ru.yoomoney.sdk.kassa.payments.contract.di.ContractModule
import ru.yoomoney.sdk.kassa.payments.paymentAuth.PaymentAuthFragment
import ru.yoomoney.sdk.kassa.payments.paymentAuth.di.PaymentAuthModule
import ru.yoomoney.sdk.kassa.payments.userAuth.MoneyAuthFragment
import ru.yoomoney.sdk.kassa.payments.userAuth.di.UserAuthModule
import ru.yoomoney.sdk.kassa.payments.contract.ContractFragment
import ru.yoomoney.sdk.kassa.payments.paymentOptionList.PaymentOptionListFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.TokenizeFragment
import ru.yoomoney.sdk.kassa.payments.tokenize.di.TokenizeModule
import ru.yoomoney.sdk.kassa.payments.ui.view.BankCardView
import ru.yoomoney.sdk.kassa.payments.unbind.UnbindCardFragment
import ru.yoomoney.sdk.kassa.payments.unbind.di.UnbindCardModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ViewModelKeyedFactoryModule::class,
        SharedPreferencesModule::class,
        TokenStorageModule::class,
        TokensStorageModule::class,
        CurrentUserModule::class,
        YandexMetricaReporterModule::class,
        ReporterModule::class,
        OkHttpModule::class,
        HttpModule::class,
        PaymentModule::class,
        CoreModule::class,
        UserAuthModule::class,
        ContractModule::class,
        PaymentAuthModule::class,
        ConfigModule::class,
        UnbindCardModule::class,
        TokenizeModule::class
    ]
)
internal interface CheckoutActivityComponent {

    fun inject(activity: CheckoutActivity)

    fun inject(paymentOptionListFragment: PaymentOptionListFragment)

    fun inject(mainDialogFragment: MainDialogFragment)

    fun inject(contractFragment: ContractFragment)

    fun inject(contractFragment: TokenizeFragment)

    fun inject(unbindCardFragment: UnbindCardFragment)

    fun inject(paymentAuthFragment: PaymentAuthFragment)

    fun inject(moneyAuthFragment: MoneyAuthFragment)

    fun inject(bankCardView: BankCardView)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun paymentParameters(paymentParameters: PaymentParameters): Builder

        @BindsInstance
        fun testParameters(testParameters: TestParameters): Builder

        @BindsInstance
        fun uiParameters(uiParameters: UiParameters): Builder

        @BindsInstance
        fun paymentMethodId(paymentMethodId: PaymentMethodId?): Builder

        fun okHttpModule(okHttpModule: OkHttpModule): Builder

        fun paymentOptionsListModule(paymentOptionsListModule: PaymentOptionsListModule): Builder

        fun mainReporterModule(yandexMetricaReporterModule: YandexMetricaReporterModule): Builder

        fun tokensStorageModule(tokensStorageModule: TokensStorageModule): Builder

        fun currentUserModule(currentUserModule: CurrentUserModule): Builder

        fun build(): CheckoutActivityComponent
    }
}