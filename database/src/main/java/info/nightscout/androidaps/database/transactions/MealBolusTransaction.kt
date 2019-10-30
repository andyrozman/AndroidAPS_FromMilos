package info.nightscout.androidaps.database.transactions

import info.nightscout.androidaps.database.entities.Bolus
import info.nightscout.androidaps.database.entities.Carbs
import info.nightscout.androidaps.database.entities.links.MealLink
import java.util.*

/**
 * Inserts a Bolus, Carbs, a BolusCalculatorResult and a MealLink into the database
 */
class MealBolusTransaction(
        val timestamp: Long,
        val insulin: Double,
        val carbs: Double,
        val type: Bolus.Type,
        val carbTime: Long = 0,
        val bolusCalculatorResult: BolusCalculatorResult? = null
) : Transaction<Unit>() {

    override fun run() {
        var entries = 0
        val utcOffset = TimeZone.getDefault().getOffset(timestamp).toLong()
        val bolusDBId = if (insulin > 0) {
            entries += 1
            database.bolusDao.insertNewEntry(Bolus(
                    timestamp = timestamp,
                    utcOffset = utcOffset,
                    amount = insulin,
                    type = type,
                    isBasalInsulin = false
            ))
        } else {
            null
        }
        val carbsDBId = if (carbs > 0) {
            entries += 1
            database.carbsDao.insertNewEntry(Carbs(
                    timestamp = timestamp + carbTime,
                    utcOffset = utcOffset,
                    amount = carbs,
                    duration = 0
            ))
        } else {
            null
        }
        val bolusCalculatorResultDBId = if (bolusCalculatorResult != null) {
            entries += 1
            database.bolusCalculatorResultDao.insertNewEntry(info.nightscout.androidaps.database.entities.BolusCalculatorResult(
                    timestamp = timestamp,
                    utcOffset = utcOffset,
                    targetBGLow = bolusCalculatorResult.targetBGLow,
                    targetBGHigh = bolusCalculatorResult.targetBGHigh,
                    isf = bolusCalculatorResult.isf,
                    ic = bolusCalculatorResult.ic,
                    bolusIOB = bolusCalculatorResult.bolusIOB,
                    wasBolusIOBUsed = bolusCalculatorResult.bolusIOBUsed,
                    basalIOB = bolusCalculatorResult.basalIOB,
                    wasBasalIOBUsed = bolusCalculatorResult.basalIOBUsed,
                    glucoseValue = bolusCalculatorResult.glucoseValue,
                    wasGlucoseUsed = bolusCalculatorResult.glucoseUsed,
                    glucoseDifference = bolusCalculatorResult.glucoseDifference,
                    glucoseInsulin = bolusCalculatorResult.glucoseInsulin,
                    glucoseTrend = bolusCalculatorResult.glucoseTrend,
                    wasTrendUsed = bolusCalculatorResult.trendUsed,
                    trendInsulin = bolusCalculatorResult.trendInsulin,
                    carbs = bolusCalculatorResult.carbs,
                    wereCarbsUsed = bolusCalculatorResult.carbsUsed,
                    carbsInsulin = bolusCalculatorResult.carbsInsulin,
                    otherCorrection = bolusCalculatorResult.otherCorrection,
                    wasSuperbolusUsed = bolusCalculatorResult.superbolusUsed,
                    superbolusInsulin = bolusCalculatorResult.superbolusInsulin,
                    wasTempTargetUsed = bolusCalculatorResult.tempTargetUsed,
                    totalInsulin = bolusCalculatorResult.totalInsulin,
                    cob = bolusCalculatorResult.cob,
                    wasCOBUsed = bolusCalculatorResult.cobUsed,
                    cobInsulin = bolusCalculatorResult.cobInsulin
            ))
        } else {
            null
        }
        if (entries > 1) {
            database.mealLinkDao.insertNewEntry(MealLink(
                    bolusId = bolusDBId,
                    carbsId = carbsDBId,
                    bolusCalcResultId = bolusCalculatorResultDBId
            ))
        }
    }

    data class BolusCalculatorResult(
            var targetBGLow: Double,
            var targetBGHigh: Double,
            var isf: Double,
            var ic: Double,
            var bolusIOB: Double,
            var bolusIOBUsed: Boolean,
            var basalIOB: Double,
            var basalIOBUsed: Boolean,
            var glucoseValue: Double,
            var glucoseUsed: Boolean,
            var glucoseDifference: Double,
            var glucoseInsulin: Double,
            var glucoseTrend: Double,
            var trendUsed: Boolean,
            var trendInsulin: Double,
            var cob: Double,
            var cobUsed: Boolean,
            var cobInsulin: Double,
            var carbs: Double,
            var carbsUsed: Boolean,
            var carbsInsulin: Double,
            var otherCorrection: Double,
            var superbolusUsed: Boolean,
            var superbolusInsulin: Double,
            var tempTargetUsed: Boolean,
            var totalInsulin: Double
    )
}