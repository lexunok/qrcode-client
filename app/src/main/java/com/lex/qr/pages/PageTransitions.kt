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
import com.lex.qr.viewmodels.CurrentClassesPage
import com.lex.qr.viewmodels.CurrentCodeFormPage
import com.lex.qr.viewmodels.CurrentLoginPage
import com.lex.qr.viewmodels.CurrentStaffPage
import com.lex.qr.viewmodels.CurrentStatisticsPage
import com.lex.qr.viewmodels.admin.CurrentArchivePage
import com.lex.qr.viewmodels.admin.CurrentEditorPage

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
                from == CurrentAdminPage.Main && to == CurrentAdminPage.Editor -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.Main && to == CurrentAdminPage.Create -> PageTransitionDirection.DOWN
                from == CurrentAdminPage.Main && to == CurrentAdminPage.Archive -> PageTransitionDirection.RIGHT

                from == CurrentAdminPage.Editor && to == CurrentAdminPage.Main -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.Editor && to == CurrentAdminPage.Create -> PageTransitionDirection.RIGHT
                from == CurrentAdminPage.Editor && to == CurrentAdminPage.Archive -> PageTransitionDirection.RIGHT

                from == CurrentAdminPage.Create && to == CurrentAdminPage.Editor -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.Create && to == CurrentAdminPage.Main -> PageTransitionDirection.UP
                from == CurrentAdminPage.Create && to == CurrentAdminPage.Archive -> PageTransitionDirection.RIGHT

                from == CurrentAdminPage.Archive && to == CurrentAdminPage.Editor -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.Archive && to == CurrentAdminPage.Main -> PageTransitionDirection.LEFT
                from == CurrentAdminPage.Archive && to == CurrentAdminPage.Create -> PageTransitionDirection.LEFT
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is CurrentEditorPage && to is CurrentEditorPage -> {
            when{
                from == CurrentEditorPage.Category && to == CurrentEditorPage.ObjectList -> PageTransitionDirection.LEFT

                from == CurrentEditorPage.ObjectList && to == CurrentEditorPage.Editor -> PageTransitionDirection.LEFT
                from == CurrentEditorPage.ObjectList && to == CurrentEditorPage.Category -> PageTransitionDirection.RIGHT

                from == CurrentEditorPage.Editor && to == CurrentEditorPage.ObjectList -> PageTransitionDirection.RIGHT
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is CurrentArchivePage && to is CurrentArchivePage -> {
            when{
                from == CurrentArchivePage.Semesters && to == CurrentArchivePage.Semester -> PageTransitionDirection.RIGHT

                from == CurrentArchivePage.Semester && to == CurrentArchivePage.Subjects -> PageTransitionDirection.RIGHT
                from == CurrentArchivePage.Semester && to == CurrentArchivePage.Groups -> PageTransitionDirection.RIGHT
                from == CurrentArchivePage.Semester && to == CurrentArchivePage.Semesters -> PageTransitionDirection.LEFT

                from == CurrentArchivePage.Subjects && to == CurrentArchivePage.Semester -> PageTransitionDirection.LEFT

                from == CurrentArchivePage.Groups && to == CurrentArchivePage.Semester -> PageTransitionDirection.LEFT
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is CurrentStaffPage && to is CurrentStaffPage -> {
            when {
                from == CurrentStaffPage.Main && to == CurrentStaffPage.ClassList -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.Main && to == CurrentStaffPage.Statistics -> PageTransitionDirection.RIGHT
                from == CurrentStaffPage.Main && to == CurrentStaffPage.CodeForm -> PageTransitionDirection.LEFT
                from == CurrentStaffPage.Main && to == CurrentStaffPage.Activity -> PageTransitionDirection.UP

                from == CurrentStaffPage.ClassList && to == CurrentStaffPage.Main -> PageTransitionDirection.RIGHT

                from == CurrentStaffPage.Statistics && to == CurrentStaffPage.Main -> PageTransitionDirection.LEFT

                from == CurrentStaffPage.CodeForm && to == CurrentStaffPage.Main -> PageTransitionDirection.RIGHT

                from == CurrentStaffPage.Activity && to == CurrentStaffPage.Main -> PageTransitionDirection.DOWN
                else -> PageTransitionDirection.RIGHT
            }
        }
        from is CurrentCodeFormPage && to is CurrentCodeFormPage -> {
            when {
                from == CurrentCodeFormPage.SubjectList && to == CurrentCodeFormPage.GroupList -> PageTransitionDirection.LEFT

                from == CurrentCodeFormPage.GroupList && to == CurrentCodeFormPage.LifeTime -> PageTransitionDirection.LEFT
                from == CurrentCodeFormPage.GroupList && to == CurrentCodeFormPage.SubjectList -> PageTransitionDirection.RIGHT

                from == CurrentCodeFormPage.LifeTime && to == CurrentCodeFormPage.GroupList -> PageTransitionDirection.RIGHT
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
        from is CurrentClassesPage && to is CurrentClassesPage -> {
            when {
                from == CurrentClassesPage.SubjectList && to == CurrentClassesPage.GroupList -> PageTransitionDirection.LEFT

                from == CurrentClassesPage.GroupList && to == CurrentClassesPage.Classes -> PageTransitionDirection.LEFT
                from == CurrentClassesPage.GroupList && to == CurrentClassesPage.SubjectList -> PageTransitionDirection.RIGHT

                from == CurrentClassesPage.Classes && to == CurrentClassesPage.Visits -> PageTransitionDirection.LEFT
                from == CurrentClassesPage.Classes && to == CurrentClassesPage.GroupList -> PageTransitionDirection.RIGHT

                from == CurrentClassesPage.Visits && to == CurrentClassesPage.Classes -> PageTransitionDirection.RIGHT
                else -> PageTransitionDirection.RIGHT
            }
        }
        else -> PageTransitionDirection.RIGHT
    }
}