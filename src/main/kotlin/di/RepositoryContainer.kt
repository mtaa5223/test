package com.example.di

import com.example.domain.FoodProduction.Interface.IFoodProductionRepository
import com.example.domain.ProductionSpec.Interface.IProductionSpecRegistry
import com.example.domain.Upgrade.Interface.IUpgradeQueryRepository
import com.example.domain.User.Interface.ISignInCommandHandler
import com.example.infra.FoodProduction.PostgresProductionRepository
import com.example.infra.ProductionSpec.PostgresProductionSpecRegistry
import com.example.infra.Signin.PostgresSignInCommandHandler
import com.example.infra.Upgrade.PostgresUpgradeQueryRepository

open class RepositoryContainer {
    open val productionRepository: IFoodProductionRepository = PostgresProductionRepository()
    open val upgradeQueryRepository: IUpgradeQueryRepository = PostgresUpgradeQueryRepository()
    open val productionSpecRegistry: IProductionSpecRegistry = PostgresProductionSpecRegistry()
    open val signInCommandHandler: ISignInCommandHandler = PostgresSignInCommandHandler()
}
