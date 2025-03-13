package com.lex.qr.components

import com.lex.qr.pages.CurrentAdminPage
import com.lex.qr.pages.Page
import com.lex.qr.pages.PageTransitionDirection

fun getTransitionDirection(from: Page, to: Page): PageTransitionDirection {
    return when {
        from is CurrentAdminPage && to is CurrentAdminPage -> {
            when {
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.CATEGORY -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.MAIN -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.LIST && to == CurrentAdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.LIST && to == CurrentAdminPage.MAIN -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.LIST && to == CurrentAdminPage.CATEGORY -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.LIST && to == CurrentAdminPage.EDITOR -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.CATEGORY && to == CurrentAdminPage.LIST -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.CATEGORY && to == CurrentAdminPage.MAIN -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.CATEGORY && to == CurrentAdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.MAIN && to == CurrentAdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.MAIN && to == CurrentAdminPage.CATEGORY -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.CREATE && to == CurrentAdminPage.MAIN -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.CREATE && to == CurrentAdminPage.CATEGORY -> PageTransitionDirection.LEFT
                else -> PageTransitionDirection.RIGHT
            }
        }
//        from is CurrentStaffPage && to is CurrentStaffPage -> {
//            when {
//                from == CurrentStaffPage.MAIN && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.UP
//                from == CurrentStaffPage.SUBJECT && to == CurrentStaffPage.GROUP -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.MAIN -> PageTransitionDirection.DOWN
//                else -> PageTransitionDirection.RIGHT
//            }
//        }
        else -> PageTransitionDirection.RIGHT
    }
}