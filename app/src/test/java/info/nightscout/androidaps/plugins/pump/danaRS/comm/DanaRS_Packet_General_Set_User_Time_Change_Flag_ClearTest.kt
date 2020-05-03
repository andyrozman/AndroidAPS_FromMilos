package info.nightscout.androidaps.plugins.pump.danaRS.comm

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest()
class DanaRS_Packet_General_Set_User_Time_Change_Flag_ClearTest : DanaRSTestBase() {

    @Test fun runTest() {
        val packet = DanaRS_Packet_General_Set_User_Time_Change_Flag_Clear(aapsLogger)
        // test params
        Assert.assertEquals(null, packet.requestParams)
        // test message decoding
        packet.handleMessage(createArray(3, 0.toByte()))
        Assert.assertEquals(false, packet.failed)
        // everything ok :)
        packet.handleMessage(createArray(17, 1.toByte()))
        Assert.assertEquals(true, packet.failed)
        Assert.assertEquals("REVIEW__SET_USER_TIME_CHANGE_FLAG_CLEAR", packet.friendlyName)
    }
}