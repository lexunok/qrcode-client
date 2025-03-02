package com.lex.qr.components

import com.lex.qr.pages.AdminPage
import com.lex.qr.pages.Page
import com.lex.qr.pages.PageTransitionDirection
import com.lex.qr.pages.StaffPage

fun getTransitionDirection(from: Page, to: Page): PageTransitionDirection {
    return when {
        from is AdminPage && to is AdminPage -> {
            when {
                from == AdminPage.MAIN && to == AdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == AdminPage.MAIN && to == AdminPage.LIST -> PageTransitionDirection.LEFT
                from == AdminPage.CREATE && to == AdminPage.MAIN -> PageTransitionDirection.LEFT
                from == AdminPage.CREATE && to == AdminPage.LIST -> PageTransitionDirection.LEFT
                from == AdminPage.LIST && to == AdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == AdminPage.LIST && to == AdminPage.MAIN -> PageTransitionDirection.RIGHT
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is StaffPage && to is StaffPage -> {
            when {
                from == StaffPage.MAIN && to == StaffPage.SUBJECT -> PageTransitionDirection.UP
                from == StaffPage.SUBJECT && to == StaffPage.GROUP -> PageTransitionDirection.RIGHT
                from == StaffPage.GROUP && to == StaffPage.MAIN -> PageTransitionDirection.DOWN
                else -> PageTransitionDirection.RIGHT
            }
        }
        else -> PageTransitionDirection.RIGHT
    }
}