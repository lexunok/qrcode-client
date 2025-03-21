package com.lex.qr.components

import com.lex.qr.pages.CurrentAdminPage
import com.lex.qr.pages.CurrentStaffPage
import com.lex.qr.pages.Page
import com.lex.qr.pages.PageTransitionDirection

fun getTransitionDirection(from: Page, to: Page): PageTransitionDirection {
    return when {
        from is CurrentAdminPage && to is CurrentAdminPage -> {
            when {
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.CATEGORY -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.MAIN -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.CREATE -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.EDITOR && to == CurrentAdminPage.SELECT_USER_GROUP -> PageTransitionDirection.LEFT
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
                from == CurrentAdminPage.CREATE && to == CurrentAdminPage.SELECT_USER_GROUP -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.SELECT_USER_GROUP && to == CurrentAdminPage.CREATE -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.SELECT_USER_GROUP && to == CurrentAdminPage.MAIN -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.SELECT_USER_GROUP && to == CurrentAdminPage.CATEGORY -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.SELECT_USER_GROUP && to == CurrentAdminPage.EDITOR -> PageTransitionDirection.RIGHT
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is CurrentStaffPage && to is CurrentStaffPage -> {
            when {
                from == CurrentStaffPage.QRCODE && to == CurrentStaffPage.ACTIVITY -> PageTransitionDirection.UP
                from == CurrentStaffPage.QRCODE && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.QRLIVE && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
                from == CurrentStaffPage.QRLIVE && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.SUBJECT && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
                from == CurrentStaffPage.SUBJECT && to == CurrentStaffPage.GROUP -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.QRLIVE -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.CLASSES -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.CLASSES && to == CurrentStaffPage.VISITS -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.CLASSES && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
                from == CurrentStaffPage.VISITS && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
                from == CurrentStaffPage.ACTIVITY && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.DOWN
                from == CurrentStaffPage.ACTIVITY && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.RIGHT
                else -> PageTransitionDirection.RIGHT
            }
        }
        else -> PageTransitionDirection.RIGHT
    }
}