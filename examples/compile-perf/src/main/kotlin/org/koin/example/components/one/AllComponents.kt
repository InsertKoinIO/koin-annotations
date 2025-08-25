package org.koin.example.components.one

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
class MyModule

@Single
class ComponentA1
@Single
class ComponentB1(val a : ComponentA1)
@Single
class ComponentC1(val a : ComponentA1, val b : ComponentB1)
@Single
class ComponentA2
@Single
class ComponentB2(val a : ComponentA2)
@Single
class ComponentC2(val a : ComponentA2, val b : ComponentB2)
@Single
class ComponentA3
@Single
class ComponentB3(val a : ComponentA3)
@Single
class ComponentC3(val a : ComponentA3, val b : ComponentB3)
@Single
class ComponentA4
@Single
class ComponentB4(val a : ComponentA4)
@Single
class ComponentC4(val a : ComponentA4, val b : ComponentB4)
@Single
class ComponentA5
@Single
class ComponentB5(val a : ComponentA5)
@Single
class ComponentC5(val a : ComponentA5, val b : ComponentB5)
@Single
class ComponentA6
@Single
class ComponentB6(val a : ComponentA6)
@Single
class ComponentC6(val a : ComponentA6, val b : ComponentB6)
@Single
class ComponentA7
@Single
class ComponentB7(val a : ComponentA7)
@Single
class ComponentC7(val a : ComponentA7, val b : ComponentB7)
@Single
class ComponentA8
@Single
class ComponentB8(val a : ComponentA8)
@Single
class ComponentC8(val a : ComponentA8, val b : ComponentB8)
@Single
class ComponentA9
@Single
class ComponentB9(val a : ComponentA9)
@Single
class ComponentC9(val a : ComponentA9, val b : ComponentB9)
@Single
class ComponentA10
@Single
class ComponentB10(val a : ComponentA10)
@Single
class ComponentC10(val a : ComponentA10, val b : ComponentB10)
@Single
class ComponentA11
@Single
class ComponentB11(val a : ComponentA11)
@Single
class ComponentC11(val a : ComponentA11, val b : ComponentB11)
@Single
class ComponentA12
@Single
class ComponentB12(val a : ComponentA12)
@Single
class ComponentC12(val a : ComponentA12, val b : ComponentB12)
@Single
class ComponentA13
@Single
class ComponentB13(val a : ComponentA13)
@Single
class ComponentC13(val a : ComponentA13, val b : ComponentB13)
@Single
class ComponentA14
@Single
class ComponentB14(val a : ComponentA14)
@Single
class ComponentC14(val a : ComponentA14, val b : ComponentB14)
@Single
class ComponentA15
@Single
class ComponentB15(val a : ComponentA15)
@Single
class ComponentC15(val a : ComponentA15, val b : ComponentB15)
@Single
class ComponentA16
@Single
class ComponentB16(val a : ComponentA16)
@Single
class ComponentC16(val a : ComponentA16, val b : ComponentB16)
@Single
class ComponentA17
@Single
class ComponentB17(val a : ComponentA17)
@Single
class ComponentC17(val a : ComponentA17, val b : ComponentB17)
@Single
class ComponentA18
@Single
class ComponentB18(val a : ComponentA18)
@Single
class ComponentC18(val a : ComponentA18, val b : ComponentB18)
@Single
class ComponentA19
@Single
class ComponentB19(val a : ComponentA19)
@Single
class ComponentC19(val a : ComponentA19, val b : ComponentB19)
@Single
class ComponentA20
@Single
class ComponentB20(val a : ComponentA20)
@Single
class ComponentC20(val a : ComponentA20, val b : ComponentB20)
@Single
class ComponentA21
@Single
class ComponentB21(val a : ComponentA21)
@Single
class ComponentC21(val a : ComponentA21, val b : ComponentB21)
@Single
class ComponentA22
@Single
class ComponentB22(val a : ComponentA22)
@Single
class ComponentC22(val a : ComponentA22, val b : ComponentB22)
@Single
class ComponentA23
@Single
class ComponentB23(val a : ComponentA23)
@Single
class ComponentC23(val a : ComponentA23, val b : ComponentB23)
@Single
class ComponentA24
@Single
class ComponentB24(val a : ComponentA24)
@Single
class ComponentC24(val a : ComponentA24, val b : ComponentB24)
@Single
class ComponentA25
@Single
class ComponentB25(val a : ComponentA25)
@Single
class ComponentC25(val a : ComponentA25, val b : ComponentB25)
@Single
class ComponentA26
@Single
class ComponentB26(val a : ComponentA26)
@Single
class ComponentC26(val a : ComponentA26, val b : ComponentB26)
@Single
class ComponentA27
@Single
class ComponentB27(val a : ComponentA27)
@Single
class ComponentC27(val a : ComponentA27, val b : ComponentB27)
@Single
class ComponentA28
@Single
class ComponentB28(val a : ComponentA28)
@Single
class ComponentC28(val a : ComponentA28, val b : ComponentB28)
@Single
class ComponentA29
@Single
class ComponentB29(val a : ComponentA29)
@Single
class ComponentC29(val a : ComponentA29, val b : ComponentB29)
@Single
class ComponentA30
@Single
class ComponentB30(val a : ComponentA30)
@Single
class ComponentC30(val a : ComponentA30, val b : ComponentB30)
@Single
class ComponentA31
@Single
class ComponentB31(val a : ComponentA31)
@Single
class ComponentC31(val a : ComponentA31, val b : ComponentB31)
@Single
class ComponentA32
@Single
class ComponentB32(val a : ComponentA32)
@Single
class ComponentC32(val a : ComponentA32, val b : ComponentB32)
@Single
class ComponentA33
@Single
class ComponentB33(val a : ComponentA33)
@Single
class ComponentC33(val a : ComponentA33, val b : ComponentB33)
@Single
class ComponentA34
@Single
class ComponentB34(val a : ComponentA34)
@Single
class ComponentC34(val a : ComponentA34, val b : ComponentB34)
@Single
class ComponentA35
@Single
class ComponentB35(val a : ComponentA35)
@Single
class ComponentC35(val a : ComponentA35, val b : ComponentB35)
@Single
class ComponentA36
@Single
class ComponentB36(val a : ComponentA36)
@Single
class ComponentC36(val a : ComponentA36, val b : ComponentB36)
@Single
class ComponentA37
@Single
class ComponentB37(val a : ComponentA37)
@Single
class ComponentC37(val a : ComponentA37, val b : ComponentB37)
@Single
class ComponentA38
@Single
class ComponentB38(val a : ComponentA38)
@Single
class ComponentC38(val a : ComponentA38, val b : ComponentB38)
@Single
class ComponentA39
@Single
class ComponentB39(val a : ComponentA39)
@Single
class ComponentC39(val a : ComponentA39, val b : ComponentB39)
@Single
class ComponentA40
@Single
class ComponentB40(val a : ComponentA40)
@Single
class ComponentC40(val a : ComponentA40, val b : ComponentB40)
@Single
class ComponentA41
@Single
class ComponentB41(val a : ComponentA41)
@Single
class ComponentC41(val a : ComponentA41, val b : ComponentB41)
@Single
class ComponentA42
@Single
class ComponentB42(val a : ComponentA42)
@Single
class ComponentC42(val a : ComponentA42, val b : ComponentB42)
@Single
class ComponentA43
@Single
class ComponentB43(val a : ComponentA43)
@Single
class ComponentC43(val a : ComponentA43, val b : ComponentB43)
@Single
class ComponentA44
@Single
class ComponentB44(val a : ComponentA44)
@Single
class ComponentC44(val a : ComponentA44, val b : ComponentB44)
@Single
class ComponentA45
@Single
class ComponentB45(val a : ComponentA45)
@Single
class ComponentC45(val a : ComponentA45, val b : ComponentB45)
@Single
class ComponentA46
@Single
class ComponentB46(val a : ComponentA46)
@Single
class ComponentC46(val a : ComponentA46, val b : ComponentB46)
@Single
class ComponentA47
@Single
class ComponentB47(val a : ComponentA47)
@Single
class ComponentC47(val a : ComponentA47, val b : ComponentB47)
@Single
class ComponentA48
@Single
class ComponentB48(val a : ComponentA48)
@Single
class ComponentC48(val a : ComponentA48, val b : ComponentB48)
@Single
class ComponentA49
@Single
class ComponentB49(val a : ComponentA49)
@Single
class ComponentC49(val a : ComponentA49, val b : ComponentB49)
@Single
class ComponentA50
@Single
class ComponentB50(val a : ComponentA50)
@Single
class ComponentC50(val a : ComponentA50, val b : ComponentB50)
@Single
class ComponentA51
@Single
class ComponentB51(val a : ComponentA51)
@Single
class ComponentC51(val a : ComponentA51, val b : ComponentB51)
@Single
class ComponentA52
@Single
class ComponentB52(val a : ComponentA52)
@Single
class ComponentC52(val a : ComponentA52, val b : ComponentB52)
@Single
class ComponentA53
@Single
class ComponentB53(val a : ComponentA53)
@Single
class ComponentC53(val a : ComponentA53, val b : ComponentB53)
@Single
class ComponentA54
@Single
class ComponentB54(val a : ComponentA54)
@Single
class ComponentC54(val a : ComponentA54, val b : ComponentB54)
@Single
class ComponentA55
@Single
class ComponentB55(val a : ComponentA55)
@Single
class ComponentC55(val a : ComponentA55, val b : ComponentB55)
@Single
class ComponentA56
@Single
class ComponentB56(val a : ComponentA56)
@Single
class ComponentC56(val a : ComponentA56, val b : ComponentB56)
@Single
class ComponentA57
@Single
class ComponentB57(val a : ComponentA57)
@Single
class ComponentC57(val a : ComponentA57, val b : ComponentB57)
@Single
class ComponentA58
@Single
class ComponentB58(val a : ComponentA58)
@Single
class ComponentC58(val a : ComponentA58, val b : ComponentB58)
@Single
class ComponentA59
@Single
class ComponentB59(val a : ComponentA59)
@Single
class ComponentC59(val a : ComponentA59, val b : ComponentB59)
@Single
class ComponentA60
@Single
class ComponentB60(val a : ComponentA60)
@Single
class ComponentC60(val a : ComponentA60, val b : ComponentB60)
@Single
class ComponentA61
@Single
class ComponentB61(val a : ComponentA61)
@Single
class ComponentC61(val a : ComponentA61, val b : ComponentB61)
@Single
class ComponentA62
@Single
class ComponentB62(val a : ComponentA62)
@Single
class ComponentC62(val a : ComponentA62, val b : ComponentB62)
@Single
class ComponentA63
@Single
class ComponentB63(val a : ComponentA63)
@Single
class ComponentC63(val a : ComponentA63, val b : ComponentB63)
@Single
class ComponentA64
@Single
class ComponentB64(val a : ComponentA64)
@Single
class ComponentC64(val a : ComponentA64, val b : ComponentB64)
@Single
class ComponentA65
@Single
class ComponentB65(val a : ComponentA65)
@Single
class ComponentC65(val a : ComponentA65, val b : ComponentB65)
@Single
class ComponentA66
@Single
class ComponentB66(val a : ComponentA66)
@Single
class ComponentC66(val a : ComponentA66, val b : ComponentB66)
@Single
class ComponentA67
@Single
class ComponentB67(val a : ComponentA67)
@Single
class ComponentC67(val a : ComponentA67, val b : ComponentB67)
@Single
class ComponentA68
@Single
class ComponentB68(val a : ComponentA68)
@Single
class ComponentC68(val a : ComponentA68, val b : ComponentB68)
@Single
class ComponentA69
@Single
class ComponentB69(val a : ComponentA69)
@Single
class ComponentC69(val a : ComponentA69, val b : ComponentB69)
@Single
class ComponentA70
@Single
class ComponentB70(val a : ComponentA70)
@Single
class ComponentC70(val a : ComponentA70, val b : ComponentB70)
@Single
class ComponentA71
@Single
class ComponentB71(val a : ComponentA71)
@Single
class ComponentC71(val a : ComponentA71, val b : ComponentB71)
@Single
class ComponentA72
@Single
class ComponentB72(val a : ComponentA72)
@Single
class ComponentC72(val a : ComponentA72, val b : ComponentB72)
@Single
class ComponentA73
@Single
class ComponentB73(val a : ComponentA73)
@Single
class ComponentC73(val a : ComponentA73, val b : ComponentB73)
@Single
class ComponentA74
@Single
class ComponentB74(val a : ComponentA74)
@Single
class ComponentC74(val a : ComponentA74, val b : ComponentB74)
@Single
class ComponentA75
@Single
class ComponentB75(val a : ComponentA75)
@Single
class ComponentC75(val a : ComponentA75, val b : ComponentB75)
@Single
class ComponentA76
@Single
class ComponentB76(val a : ComponentA76)
@Single
class ComponentC76(val a : ComponentA76, val b : ComponentB76)
@Single
class ComponentA77
@Single
class ComponentB77(val a : ComponentA77)
@Single
class ComponentC77(val a : ComponentA77, val b : ComponentB77)
@Single
class ComponentA78
@Single
class ComponentB78(val a : ComponentA78)
@Single
class ComponentC78(val a : ComponentA78, val b : ComponentB78)
@Single
class ComponentA79
@Single
class ComponentB79(val a : ComponentA79)
@Single
class ComponentC79(val a : ComponentA79, val b : ComponentB79)
@Single
class ComponentA80
@Single
class ComponentB80(val a : ComponentA80)
@Single
class ComponentC80(val a : ComponentA80, val b : ComponentB80)
@Single
class ComponentA81
@Single
class ComponentB81(val a : ComponentA81)
@Single
class ComponentC81(val a : ComponentA81, val b : ComponentB81)
@Single
class ComponentA82
@Single
class ComponentB82(val a : ComponentA82)
@Single
class ComponentC82(val a : ComponentA82, val b : ComponentB82)
@Single
class ComponentA83
@Single
class ComponentB83(val a : ComponentA83)
@Single
class ComponentC83(val a : ComponentA83, val b : ComponentB83)
@Single
class ComponentA84
@Single
class ComponentB84(val a : ComponentA84)
@Single
class ComponentC84(val a : ComponentA84, val b : ComponentB84)
@Single
class ComponentA85
@Single
class ComponentB85(val a : ComponentA85)
@Single
class ComponentC85(val a : ComponentA85, val b : ComponentB85)
@Single
class ComponentA86
@Single
class ComponentB86(val a : ComponentA86)
@Single
class ComponentC86(val a : ComponentA86, val b : ComponentB86)
@Single
class ComponentA87
@Single
class ComponentB87(val a : ComponentA87)
@Single
class ComponentC87(val a : ComponentA87, val b : ComponentB87)
@Single
class ComponentA88
@Single
class ComponentB88(val a : ComponentA88)
@Single
class ComponentC88(val a : ComponentA88, val b : ComponentB88)
@Single
class ComponentA89
@Single
class ComponentB89(val a : ComponentA89)
@Single
class ComponentC89(val a : ComponentA89, val b : ComponentB89)
@Single
class ComponentA90
@Single
class ComponentB90(val a : ComponentA90)
@Single
class ComponentC90(val a : ComponentA90, val b : ComponentB90)
@Single
class ComponentA91
@Single
class ComponentB91(val a : ComponentA91)
@Single
class ComponentC91(val a : ComponentA91, val b : ComponentB91)
@Single
class ComponentA92
@Single
class ComponentB92(val a : ComponentA92)
@Single
class ComponentC92(val a : ComponentA92, val b : ComponentB92)
@Single
class ComponentA93
@Single
class ComponentB93(val a : ComponentA93)
@Single
class ComponentC93(val a : ComponentA93, val b : ComponentB93)
@Single
class ComponentA94
@Single
class ComponentB94(val a : ComponentA94)
@Single
class ComponentC94(val a : ComponentA94, val b : ComponentB94)
@Single
class ComponentA95
@Single
class ComponentB95(val a : ComponentA95)
@Single
class ComponentC95(val a : ComponentA95, val b : ComponentB95)
@Single
class ComponentA96
@Single
class ComponentB96(val a : ComponentA96)
@Single
class ComponentC96(val a : ComponentA96, val b : ComponentB96)
@Single
class ComponentA97
@Single
class ComponentB97(val a : ComponentA97)
@Single
class ComponentC97(val a : ComponentA97, val b : ComponentB97)
@Single
class ComponentA98
@Single
class ComponentB98(val a : ComponentA98)
@Single
class ComponentC98(val a : ComponentA98, val b : ComponentB98)
@Single
class ComponentA99
@Single
class ComponentB99(val a : ComponentA99)
@Single
class ComponentC99(val a : ComponentA99, val b : ComponentB99)
@Single
class ComponentA100
@Single
class ComponentB100(val a : ComponentA100)
@Single
class ComponentC100(val a : ComponentA100, val b : ComponentB100)

