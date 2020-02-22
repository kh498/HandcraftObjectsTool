package no.uib.inf219.gui

import javafx.scene.text.FontWeight
import javafx.stage.Screen
import tornadofx.*

/**
 * @author Elg
 */
class Styles : Stylesheet() {
    companion object {

        const val X1_DPI = 108 // dpi of screen originally implemented on
        val scale = Screen.getPrimary().dpi / X1_DPI

        val headLineLabel by cssclass()
        val largefont by cssclass()
        val parent by cssclass()
        val numberChanger by cssclass()

        val monospaceFont = loadFont("/fonts/ubuntu/UbuntuMono-R.ttf", -1)!!
    }

    init {
        star {
            wrapText = true
            fontSize = 1.ems
        }

        button {
            padding = box(0.4.ems, 0.5.ems)
        }

        headLineLabel {
            fontSize = 2.ems
            fontWeight = FontWeight.BOLD
        }

        largefont {
            fontSize = 2.ems
        }

        /**
         * Generic rule for stuff that has multiple other elements within them
         */
        parent {
            padding = box(0.333.ems)
            spacing = 0.333.ems
        }

        splitPaneDivider {
            padding = box(0.005.ems)
        }
        numberChanger {
            font = monospaceFont
            fontSize = 0.1.ems
            padding = box(0.ems, 0.333.ems)
        }
    }
}


//em scaled
val Number.ems: Dimension<Dimension.LinearUnits>
    get() = Dimension(
        this.toDouble() * Styles.scale,
        Dimension.LinearUnits.em
    )
