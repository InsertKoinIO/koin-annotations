package org.koin.example.components.three

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
class MyModule3

@Single
class ComponentAAA1
@Single
class ComponentBBB1(val a : ComponentAAA1)
@Single
class ComponentCCC(val a : ComponentAAA1, val b : ComponentBBB1)
@Single
class ComponentAAA2
@Single
class ComponentBBB2(val a : ComponentAAA2)
@Single
class ComponentCCC2(val a : ComponentAAA2, val b : ComponentBBB2)
@Single
class ComponentAAA3
@Single
class ComponentBBB3(val a : ComponentAAA3)
@Single
class ComponentCCC3(val a : ComponentAAA3, val b : ComponentBBB3)
@Single
class ComponentAAA4
@Single
class ComponentBBB4(val a : ComponentAAA4)
@Single
class ComponentCCC4(val a : ComponentAAA4, val b : ComponentBBB4)
@Single
class ComponentAAA5
@Single
class ComponentBBB5(val a : ComponentAAA5)
@Single
class ComponentCCC5(val a : ComponentAAA5, val b : ComponentBBB5)
@Single
class ComponentAAA6
@Single
class ComponentBBB6(val a : ComponentAAA6)
@Single
class ComponentCCC6(val a : ComponentAAA6, val b : ComponentBBB6)
@Single
class ComponentAAA7
@Single
class ComponentBBB7(val a : ComponentAAA7)
@Single
class ComponentCCC7(val a : ComponentAAA7, val b : ComponentBBB7)
@Single
class ComponentAAA8
@Single
class ComponentBBB8(val a : ComponentAAA8)
@Single
class ComponentCCC8(val a : ComponentAAA8, val b : ComponentBBB8)
@Single
class ComponentAAA9
@Single
class ComponentBBB9(val a : ComponentAAA9)
@Single
class ComponentCCC9(val a : ComponentAAA9, val b : ComponentBBB9)
@Single
class ComponentAAA10
@Single
class ComponentBBB10(val a : ComponentAAA10)
@Single
class ComponentCCCC0(val a : ComponentAAA10, val b : ComponentBBB10)
@Single
class ComponentAAA11
@Single
class ComponentBBB11(val a : ComponentAAA11)
@Single
class ComponentCCCC1(val a : ComponentAAA11, val b : ComponentBBB11)
@Single
class ComponentAAA12
@Single
class ComponentBBB12(val a : ComponentAAA12)
@Single
class ComponentCCCC2(val a : ComponentAAA12, val b : ComponentBBB12)
@Single
class ComponentAAA13
@Single
class ComponentBBB13(val a : ComponentAAA13)
@Single
class ComponentCCCC3(val a : ComponentAAA13, val b : ComponentBBB13)
@Single
class ComponentAAA14
@Single
class ComponentBBB14(val a : ComponentAAA14)
@Single
class ComponentCCCC4(val a : ComponentAAA14, val b : ComponentBBB14)
@Single
class ComponentAAA15
@Single
class ComponentBBB15(val a : ComponentAAA15)
@Single
class ComponentCCCC5(val a : ComponentAAA15, val b : ComponentBBB15)
@Single
class ComponentAAA16
@Single
class ComponentBBB16(val a : ComponentAAA16)
@Single
class ComponentCCCC6(val a : ComponentAAA16, val b : ComponentBBB16)
@Single
class ComponentAAA17
@Single
class ComponentBBB17(val a : ComponentAAA17)
@Single
class ComponentCCCC7(val a : ComponentAAA17, val b : ComponentBBB17)
@Single
class ComponentAAA18
@Single
class ComponentBBB18(val a : ComponentAAA18)
@Single
class ComponentCCCC8(val a : ComponentAAA18, val b : ComponentBBB18)
@Single
class ComponentAAA19
@Single
class ComponentBBB19(val a : ComponentAAA19)
@Single
class ComponentCCCC9(val a : ComponentAAA19, val b : ComponentBBB19)
@Single
class ComponentAAA20
@Single
class ComponentBBB20(val a : ComponentAAA20)
@Single
class ComponentCCC20(val a : ComponentAAA20, val b : ComponentBBB20)
@Single
class ComponentAAA21
@Single
class ComponentBBB21(val a : ComponentAAA21)
@Single
class ComponentCCC21(val a : ComponentAAA21, val b : ComponentBBB21)
@Single
class ComponentAAA22
@Single
class ComponentBBB22(val a : ComponentAAA22)
@Single
class ComponentCCC22(val a : ComponentAAA22, val b : ComponentBBB22)
@Single
class ComponentAAA23
@Single
class ComponentBBB23(val a : ComponentAAA23)
@Single
class ComponentCCC23(val a : ComponentAAA23, val b : ComponentBBB23)
@Single
class ComponentAAA24
@Single
class ComponentBBB24(val a : ComponentAAA24)
@Single
class ComponentCCC24(val a : ComponentAAA24, val b : ComponentBBB24)
@Single
class ComponentAAA25
@Single
class ComponentBBB25(val a : ComponentAAA25)
@Single
class ComponentCCC25(val a : ComponentAAA25, val b : ComponentBBB25)
@Single
class ComponentAAA26
@Single
class ComponentBBB26(val a : ComponentAAA26)
@Single
class ComponentCCC26(val a : ComponentAAA26, val b : ComponentBBB26)
@Single
class ComponentAAA27
@Single
class ComponentBBB27(val a : ComponentAAA27)
@Single
class ComponentCCC27(val a : ComponentAAA27, val b : ComponentBBB27)
@Single
class ComponentAAA28
@Single
class ComponentBBB28(val a : ComponentAAA28)
@Single
class ComponentCCC28(val a : ComponentAAA28, val b : ComponentBBB28)
@Single
class ComponentAAA29
@Single
class ComponentBBB29(val a : ComponentAAA29)
@Single
class ComponentCCC29(val a : ComponentAAA29, val b : ComponentBBB29)
@Single
class ComponentAAA30
@Single
class ComponentBBB30(val a : ComponentAAA30)
@Single
class ComponentCCC30(val a : ComponentAAA30, val b : ComponentBBB30)
@Single
class ComponentAAA31
@Single
class ComponentBBB31(val a : ComponentAAA31)
@Single
class ComponentCCC31(val a : ComponentAAA31, val b : ComponentBBB31)
@Single
class ComponentAAA32
@Single
class ComponentBBB32(val a : ComponentAAA32)
@Single
class ComponentCCC32(val a : ComponentAAA32, val b : ComponentBBB32)
@Single
class ComponentAAA33
@Single
class ComponentBBB33(val a : ComponentAAA33)
@Single
class ComponentCCC33(val a : ComponentAAA33, val b : ComponentBBB33)
@Single
class ComponentAAA34
@Single
class ComponentBBB34(val a : ComponentAAA34)
@Single
class ComponentCCC34(val a : ComponentAAA34, val b : ComponentBBB34)
@Single
class ComponentAAA35
@Single
class ComponentBBB35(val a : ComponentAAA35)
@Single
class ComponentCCC35(val a : ComponentAAA35, val b : ComponentBBB35)
@Single
class ComponentAAA36
@Single
class ComponentBBB36(val a : ComponentAAA36)
@Single
class ComponentCCC36(val a : ComponentAAA36, val b : ComponentBBB36)
@Single
class ComponentAAA37
@Single
class ComponentBBB37(val a : ComponentAAA37)
@Single
class ComponentCCC37(val a : ComponentAAA37, val b : ComponentBBB37)
@Single
class ComponentAAA38
@Single
class ComponentBBB38(val a : ComponentAAA38)
@Single
class ComponentCCC38(val a : ComponentAAA38, val b : ComponentBBB38)
@Single
class ComponentAAA39
@Single
class ComponentBBB39(val a : ComponentAAA39)
@Single
class ComponentCCC39(val a : ComponentAAA39, val b : ComponentBBB39)
@Single
class ComponentAAA40
@Single
class ComponentBBB40(val a : ComponentAAA40)
@Single
class ComponentCCC40(val a : ComponentAAA40, val b : ComponentBBB40)
@Single
class ComponentAAA41
@Single
class ComponentBBB41(val a : ComponentAAA41)
@Single
class ComponentCCC41(val a : ComponentAAA41, val b : ComponentBBB41)
@Single
class ComponentAAA42
@Single
class ComponentBBB42(val a : ComponentAAA42)
@Single
class ComponentCCC42(val a : ComponentAAA42, val b : ComponentBBB42)
@Single
class ComponentAAA43
@Single
class ComponentBBB43(val a : ComponentAAA43)
@Single
class ComponentCCC43(val a : ComponentAAA43, val b : ComponentBBB43)
@Single
class ComponentAAA44
@Single
class ComponentBBB44(val a : ComponentAAA44)
@Single
class ComponentCCC44(val a : ComponentAAA44, val b : ComponentBBB44)
@Single
class ComponentAAA45
@Single
class ComponentBBB45(val a : ComponentAAA45)
@Single
class ComponentCCC45(val a : ComponentAAA45, val b : ComponentBBB45)
@Single
class ComponentAAA46
@Single
class ComponentBBB46(val a : ComponentAAA46)
@Single
class ComponentCCC46(val a : ComponentAAA46, val b : ComponentBBB46)
@Single
class ComponentAAA47
@Single
class ComponentBBB47(val a : ComponentAAA47)
@Single
class ComponentCCC47(val a : ComponentAAA47, val b : ComponentBBB47)
@Single
class ComponentAAA48
@Single
class ComponentBBB48(val a : ComponentAAA48)
@Single
class ComponentCCC48(val a : ComponentAAA48, val b : ComponentBBB48)
@Single
class ComponentAAA49
@Single
class ComponentBBB49(val a : ComponentAAA49)
@Single
class ComponentCCC49(val a : ComponentAAA49, val b : ComponentBBB49)
@Single
class ComponentAAA50
@Single
class ComponentBBB50(val a : ComponentAAA50)
@Single
class ComponentCCC50(val a : ComponentAAA50, val b : ComponentBBB50)
@Single
class ComponentAAA51
@Single
class ComponentBBB51(val a : ComponentAAA51)
@Single
class ComponentCCC51(val a : ComponentAAA51, val b : ComponentBBB51)
@Single
class ComponentAAA52
@Single
class ComponentBBB52(val a : ComponentAAA52)
@Single
class ComponentCCC52(val a : ComponentAAA52, val b : ComponentBBB52)
@Single
class ComponentAAA53
@Single
class ComponentBBB53(val a : ComponentAAA53)
@Single
class ComponentCCC53(val a : ComponentAAA53, val b : ComponentBBB53)
@Single
class ComponentAAA54
@Single
class ComponentBBB54(val a : ComponentAAA54)
@Single
class ComponentCCC54(val a : ComponentAAA54, val b : ComponentBBB54)
@Single
class ComponentAAA55
@Single
class ComponentBBB55(val a : ComponentAAA55)
@Single
class ComponentCCC55(val a : ComponentAAA55, val b : ComponentBBB55)
@Single
class ComponentAAA56
@Single
class ComponentBBB56(val a : ComponentAAA56)
@Single
class ComponentCCC56(val a : ComponentAAA56, val b : ComponentBBB56)
@Single
class ComponentAAA57
@Single
class ComponentBBB57(val a : ComponentAAA57)
@Single
class ComponentCCC57(val a : ComponentAAA57, val b : ComponentBBB57)
@Single
class ComponentAAA58
@Single
class ComponentBBB58(val a : ComponentAAA58)
@Single
class ComponentCCC58(val a : ComponentAAA58, val b : ComponentBBB58)
@Single
class ComponentAAA59
@Single
class ComponentBBB59(val a : ComponentAAA59)
@Single
class ComponentCCC59(val a : ComponentAAA59, val b : ComponentBBB59)
@Single
class ComponentAAA60
@Single
class ComponentBBB60(val a : ComponentAAA60)
@Single
class ComponentCCC60(val a : ComponentAAA60, val b : ComponentBBB60)
@Single
class ComponentAAA61
@Single
class ComponentBBB61(val a : ComponentAAA61)
@Single
class ComponentCCC61(val a : ComponentAAA61, val b : ComponentBBB61)
@Single
class ComponentAAA62
@Single
class ComponentBBB62(val a : ComponentAAA62)
@Single
class ComponentCCC62(val a : ComponentAAA62, val b : ComponentBBB62)
@Single
class ComponentAAA63
@Single
class ComponentBBB63(val a : ComponentAAA63)
@Single
class ComponentCCC63(val a : ComponentAAA63, val b : ComponentBBB63)
@Single
class ComponentAAA64
@Single
class ComponentBBB64(val a : ComponentAAA64)
@Single
class ComponentCCC64(val a : ComponentAAA64, val b : ComponentBBB64)
@Single
class ComponentAAA65
@Single
class ComponentBBB65(val a : ComponentAAA65)
@Single
class ComponentCCC65(val a : ComponentAAA65, val b : ComponentBBB65)
@Single
class ComponentAAA66
@Single
class ComponentBBB66(val a : ComponentAAA66)
@Single
class ComponentCCC66(val a : ComponentAAA66, val b : ComponentBBB66)
@Single
class ComponentAAA67
@Single
class ComponentBBB67(val a : ComponentAAA67)
@Single
class ComponentCCC67(val a : ComponentAAA67, val b : ComponentBBB67)
@Single
class ComponentAAA68
@Single
class ComponentBBB68(val a : ComponentAAA68)
@Single
class ComponentCCC68(val a : ComponentAAA68, val b : ComponentBBB68)
@Single
class ComponentAAA69
@Single
class ComponentBBB69(val a : ComponentAAA69)
@Single
class ComponentCCC69(val a : ComponentAAA69, val b : ComponentBBB69)
@Single
class ComponentAAA70
@Single
class ComponentBBB70(val a : ComponentAAA70)
@Single
class ComponentCCC70(val a : ComponentAAA70, val b : ComponentBBB70)
@Single
class ComponentAAA71
@Single
class ComponentBBB71(val a : ComponentAAA71)
@Single
class ComponentCCC71(val a : ComponentAAA71, val b : ComponentBBB71)
@Single
class ComponentAAA72
@Single
class ComponentBBB72(val a : ComponentAAA72)
@Single
class ComponentCCC72(val a : ComponentAAA72, val b : ComponentBBB72)
@Single
class ComponentAAA73
@Single
class ComponentBBB73(val a : ComponentAAA73)
@Single
class ComponentCCC73(val a : ComponentAAA73, val b : ComponentBBB73)
@Single
class ComponentAAA74
@Single
class ComponentBBB74(val a : ComponentAAA74)
@Single
class ComponentCCC74(val a : ComponentAAA74, val b : ComponentBBB74)
@Single
class ComponentAAA75
@Single
class ComponentBBB75(val a : ComponentAAA75)
@Single
class ComponentCCC75(val a : ComponentAAA75, val b : ComponentBBB75)
@Single
class ComponentAAA76
@Single
class ComponentBBB76(val a : ComponentAAA76)
@Single
class ComponentCCC76(val a : ComponentAAA76, val b : ComponentBBB76)
@Single
class ComponentAAA77
@Single
class ComponentBBB77(val a : ComponentAAA77)
@Single
class ComponentCCC77(val a : ComponentAAA77, val b : ComponentBBB77)
@Single
class ComponentAAA78
@Single
class ComponentBBB78(val a : ComponentAAA78)
@Single
class ComponentCCC78(val a : ComponentAAA78, val b : ComponentBBB78)
@Single
class ComponentAAA79
@Single
class ComponentBBB79(val a : ComponentAAA79)
@Single
class ComponentCCC79(val a : ComponentAAA79, val b : ComponentBBB79)
@Single
class ComponentAAA80
@Single
class ComponentBBB80(val a : ComponentAAA80)
@Single
class ComponentCCC80(val a : ComponentAAA80, val b : ComponentBBB80)
@Single
class ComponentAAA81
@Single
class ComponentBBB81(val a : ComponentAAA81)
@Single
class ComponentCCC81(val a : ComponentAAA81, val b : ComponentBBB81)
@Single
class ComponentAAA82
@Single
class ComponentBBB82(val a : ComponentAAA82)
@Single
class ComponentCCC82(val a : ComponentAAA82, val b : ComponentBBB82)
@Single
class ComponentAAA83
@Single
class ComponentBBB83(val a : ComponentAAA83)
@Single
class ComponentCCC83(val a : ComponentAAA83, val b : ComponentBBB83)
@Single
class ComponentAAA84
@Single
class ComponentBBB84(val a : ComponentAAA84)
@Single
class ComponentCCC84(val a : ComponentAAA84, val b : ComponentBBB84)
@Single
class ComponentAAA85
@Single
class ComponentBBB85(val a : ComponentAAA85)
@Single
class ComponentCCC85(val a : ComponentAAA85, val b : ComponentBBB85)
@Single
class ComponentAAA86
@Single
class ComponentBBB86(val a : ComponentAAA86)
@Single
class ComponentCCC86(val a : ComponentAAA86, val b : ComponentBBB86)
@Single
class ComponentAAA87
@Single
class ComponentBBB87(val a : ComponentAAA87)
@Single
class ComponentCCC87(val a : ComponentAAA87, val b : ComponentBBB87)
@Single
class ComponentAAA88
@Single
class ComponentBBB88(val a : ComponentAAA88)
@Single
class ComponentCCC88(val a : ComponentAAA88, val b : ComponentBBB88)
@Single
class ComponentAAA89
@Single
class ComponentBBB89(val a : ComponentAAA89)
@Single
class ComponentCCC89(val a : ComponentAAA89, val b : ComponentBBB89)
@Single
class ComponentAAA90
@Single
class ComponentBBB90(val a : ComponentAAA90)
@Single
class ComponentCCC90(val a : ComponentAAA90, val b : ComponentBBB90)
@Single
class ComponentAAA91
@Single
class ComponentBBB91(val a : ComponentAAA91)
@Single
class ComponentCCC91(val a : ComponentAAA91, val b : ComponentBBB91)
@Single
class ComponentAAA92
@Single
class ComponentBBB92(val a : ComponentAAA92)
@Single
class ComponentCCC92(val a : ComponentAAA92, val b : ComponentBBB92)
@Single
class ComponentAAA93
@Single
class ComponentBBB93(val a : ComponentAAA93)
@Single
class ComponentCCC93(val a : ComponentAAA93, val b : ComponentBBB93)
@Single
class ComponentAAA94
@Single
class ComponentBBB94(val a : ComponentAAA94)
@Single
class ComponentCCC94(val a : ComponentAAA94, val b : ComponentBBB94)
@Single
class ComponentAAA95
@Single
class ComponentBBB95(val a : ComponentAAA95)
@Single
class ComponentCCC95(val a : ComponentAAA95, val b : ComponentBBB95)
@Single
class ComponentAAA96
@Single
class ComponentBBB96(val a : ComponentAAA96)
@Single
class ComponentCCC96(val a : ComponentAAA96, val b : ComponentBBB96)
@Single
class ComponentAAA97
@Single
class ComponentBBB97(val a : ComponentAAA97)
@Single
class ComponentCCC97(val a : ComponentAAA97, val b : ComponentBBB97)
@Single
class ComponentAAA98
@Single
class ComponentBBB98(val a : ComponentAAA98)
@Single
class ComponentCCC98(val a : ComponentAAA98, val b : ComponentBBB98)
@Single
class ComponentAAA99
@Single
class ComponentBBB99(val a : ComponentAAA99)
@Single
class ComponentCCC99(val a : ComponentAAA99, val b : ComponentBBB99)
@Single
class ComponentAAA100
@Single
class ComponentBBB100(val a : ComponentAAA100)
@Single
class ComponentCCCC00(val a : ComponentAAA100, val b : ComponentBBB100)

