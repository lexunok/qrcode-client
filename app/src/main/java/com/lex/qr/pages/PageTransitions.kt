package com.lex.qr.pages

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.lex.qr.viewmodels.CurrentAdminPage
import com.lex.qr.viewmodels.CurrentLoginPage
import com.lex.qr.viewmodels.CurrentStaffPage
import com.lex.qr.viewmodels.CurrentStatisticsPage

private enum class PageTransitionDirection {
    LEFT, RIGHT, UP, DOWN
}

fun getPageTransitionSpec(
    initialState: Page,
    targetState: Page
): ContentTransform = when (getTransitionDirection(initialState, targetState)) {
    PageTransitionDirection.LEFT -> {
        (slideInHorizontally { width -> -width } + fadeIn())
            .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
    }
    PageTransitionDirection.RIGHT -> {
        (slideInHorizontally { width -> width } + fadeIn())
            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
    }
    PageTransitionDirection.UP -> {
        (slideInVertically { height -> -height } + fadeIn())
            .togetherWith(slideOutVertically { height -> height } + fadeOut())
    }
    PageTransitionDirection.DOWN -> {
        (slideInVertically { height -> height } + fadeIn())
            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
    }
}

private fun getTransitionDirection(from: Page, to: Page): PageTransitionDirection {
    return when {
        from is CurrentLoginPage && to is CurrentLoginPage -> {
            when {
                from == CurrentLoginPage.LOGIN && to == CurrentLoginPage.PASSWORD_RECOVERY -> PageTransitionDirection.RIGHT

                from == CurrentLoginPage.PASSWORD_RECOVERY && to == CurrentLoginPage.LOGIN -> PageTransitionDirection.LEFT
                from == CurrentLoginPage.PASSWORD_RECOVERY && to == CurrentLoginPage.PASSWORD_NEW -> PageTransitionDirection.RIGHT

                from == CurrentLoginPage.PASSWORD_NEW && to == CurrentLoginPage.LOGIN -> PageTransitionDirection.LEFT
                else -> PageTransitionDirection.RIGHT
            }
        }
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
        from is CurrentStaffPage && to is CurrentStaffPage -> {
            when {
                from == CurrentStaffPage.Main && to == CurrentStaffPage.Activity -> PageTransitionDirection.UP
                from == CurrentStaffPage.Main && to == CurrentStaffPage.ClassList -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.Main && to == CurrentStaffPage.Statistics -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.QRLIVE && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.QRLIVE && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.QRLIVE && to == CurrentStaffPage.STATS -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.SUBJECT && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.SUBJECT && to == CurrentStaffPage.GROUP -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.SUBJECT && to == CurrentStaffPage.STATS -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.QRLIVE -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.CLASSES -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.GROUP && to == CurrentStaffPage.STATS -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.CLASSES && to == CurrentStaffPage.VISITS -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.CLASSES && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.CLASSES && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.CLASSES && to == CurrentStaffPage.STATS -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.VISITS && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.VISITS && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.RIGHT
//                from == CurrentStaffPage.VISITS && to == CurrentStaffPage.STATS -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.ACTIVITY && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.DOWN
//                from == CurrentStaffPage.ACTIVITY && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.ACTIVITY && to == CurrentStaffPage.STATS -> PageTransitionDirection.RIGHT
//
//                from == CurrentStaffPage.STATS && to == CurrentStaffPage.QRCODE -> PageTransitionDirection.LEFT
//                from == CurrentStaffPage.STATS && to == CurrentStaffPage.SUBJECT -> PageTransitionDirection.LEFT
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is CurrentStatisticsPage && to is CurrentStatisticsPage -> {
            when {
                from == CurrentStatisticsPage.GroupList && to == CurrentStatisticsPage.StudentList -> PageTransitionDirection.RIGHT

                from == CurrentStatisticsPage.StudentList && to == CurrentStatisticsPage.UserStatistics -> PageTransitionDirection.RIGHT
                from == CurrentStatisticsPage.StudentList && to == CurrentStatisticsPage.GroupStatistics -> PageTransitionDirection.RIGHT
                from == CurrentStatisticsPage.StudentList && to == CurrentStatisticsPage.GroupList -> PageTransitionDirection.LEFT

                from == CurrentStatisticsPage.UserStatistics && to == CurrentStatisticsPage.StudentList -> PageTransitionDirection.LEFT

                from == CurrentStatisticsPage.GroupStatistics && to == CurrentStatisticsPage.StudentList -> PageTransitionDirection.LEFT
                else -> PageTransitionDirection.RIGHT
            }
        }
        else -> PageTransitionDirection.RIGHT
    }
}