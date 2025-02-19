package org.koin.example.cycle

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

//TODO uncomment to test cycle detection

//@Module
//@ComponentScan
//public class CycleModule
//
//@Single
//public class CycleA(private val b : CycleB)
//@Single
//public class CycleB(private val c : CycleC)
//@Single
//public class CycleC(private val a : CycleA)