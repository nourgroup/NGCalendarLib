package com.ngplus.ngcalendar.ui.theme

import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ngplus.ngcalendar.R
import kotlinx.coroutines.launch
import java.util.*


private val allDaysUS = listOf( "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY","SUNDAY" )
private val allDaysFR = listOf( "Lun", "Mar", "Mer", "Jeu", "Ven", "sam","Dim" )
private val mapFrenchCalendar = listOf( 7,1,2,3,4,5,6)

@Composable
fun Cal() {
    var calendarInputList by remember {
        mutableStateOf(currentDateConfiguration())
    }
    var clickedCalendarElem by remember {
        mutableStateOf<CalendarInput?>(null)
    }
    var dayState by remember {
        mutableStateOf<Day?>(Day(1, 1, 1, listOf()))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gray),
        contentAlignment = Alignment.TopCenter
    ) {
        Calendar(
            //calendarInput = calendarInputList,
            onMonthAndYearClick = {
                calendarInputList = it
            },
            onDayClick = { day ->
                Log.i("test_calendar", "${day.year}/${day.month}/${day.day}")
                clickedCalendarElem = calendarInputList.find { it.day == day }
                dayState = day
            },
            titleDate = "${dayState?.month?.plus(1)}/${dayState?.year}",
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .aspectRatio(1.3f)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.Center)
        ) {
            Text(text = "${clickedCalendarElem?.day?.day}/${clickedCalendarElem?.day?.month}/${clickedCalendarElem?.day?.year}")
            clickedCalendarElem?.day?.hours?.forEach {
                Text("$it")
            }
        }
    }
}

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    onMonthAndYearClick: (List<CalendarInput>) -> Unit,
    //calendarInput: List<CalendarInput>,
    onDayClick:(Day)->Unit,
    strokeWidth:Float = 5f,
    titleDate:String
) {
    var canvasSize by remember {
        mutableStateOf(Size.Zero)
    }
    var clickAnimationOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    var animationRadius by remember {
        mutableStateOf(0f)
    }
    var calendarInputList by remember {
        mutableStateOf(currentDateConfiguration())
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = {
            calendarInputList = nextCurrentDateConfiguration()
            onMonthAndYearClick(calendarInputList)
        }) {
            Icon(
                painter = painterResource(R.drawable.next),
                contentDescription = stringResource(id = R.string.title_activity_ng_calendar_main)
            )
        }
        Text(
            text = titleDate,
            fontWeight = FontWeight.SemiBold,
            color = white,
            fontSize = 20.sp
        )
        IconButton(onClick = {
            calendarInputList = previousCurrentDateConfiguration()
            onMonthAndYearClick(calendarInputList)
        }) {
            Icon(
                painter = painterResource(R.drawable.previous),
                contentDescription = stringResource(id = R.string.title_activity_ng_calendar_main)
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = { offset ->
                            val column =
                                (offset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
                            val row = (offset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1
                            /*
                            find the clicked day the belong to canvas
                             */
                            val indexDay = (column - 1) + (row - 1) * CALENDAR_COLUMNS
                            val selectedDay = calendarInputList[indexDay]
                            if (selectedDay.day.day <= calendarInputList.size) {
                                onDayClick(selectedDay.day)
                                clickAnimationOffset = offset
                                scope.launch {
                                    animate(0f, 225f, animationSpec = tween(300)) { value, _ ->
                                        animationRadius = value
                                    }
                                }
                            }

                        }
                    )
                }
        ){
            val canvasHeight = size.height
            val canvasWidth = size.width
            canvasSize = Size(canvasWidth,canvasHeight)
            val ySteps = canvasHeight/ CALENDAR_ROWS
            val xSteps = canvasWidth/ CALENDAR_COLUMNS

            val column = (clickAnimationOffset.x / canvasSize.width * CALENDAR_COLUMNS).toInt() + 1
            val row = (clickAnimationOffset.y / canvasSize.height * CALENDAR_ROWS).toInt() + 1

            val path = Path().apply {
                moveTo((column-1)*xSteps,(row-1)*ySteps)
                lineTo(column*xSteps,(row-1)*ySteps)
                lineTo(column*xSteps,row*ySteps)
                lineTo((column-1)*xSteps,row*ySteps)
                close()
            }

            clipPath(path){
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(orange.copy(0.8f), orange.copy(0.2f)),
                        center = clickAnimationOffset,
                        radius = animationRadius + 0.1f
                    ),
                    radius = animationRadius + 0.1f,
                    center = clickAnimationOffset
                )
            }

            drawRoundRect(
                orange,
                cornerRadius = CornerRadius(5f,5f),
                style = Stroke(
                    width = strokeWidth
                )
            )
            /*
            draw lines for row
             */
            for(i in 1 until CALENDAR_ROWS){
                drawLine(
                    color = orange,
                    start = Offset(0f,ySteps*i),
                    end = Offset(canvasWidth, ySteps*i),
                    strokeWidth = strokeWidth
                )
            }
            /*
            draw lines for column
             */
            for(i in 1 until CALENDAR_COLUMNS){
                drawLine(
                    color = orange,
                    start = Offset(xSteps*i,0f),
                    end = Offset(xSteps*i, canvasHeight),
                    strokeWidth = strokeWidth
                )
            }
            val textHeight = 17.dp.toPx()
            /*
            display days
             */
            for(i in calendarInputList.indices){
                val textPositionX = xSteps * (i% CALENDAR_COLUMNS) + strokeWidth
                val textPositionY = (i / CALENDAR_COLUMNS) * ySteps + textHeight + strokeWidth/2
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        if(calendarInputList[i].day.day==0) "" else calendarInputList[i].day.day.toString(),
                        textPositionX,
                        textPositionY,
                        Paint().apply {
                            textSize = textHeight
                            color = white.toArgb()
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}

// current date
data class CurrentDate(val month : Int,val year : Int)
var current = CurrentDate(1,1)
fun currentDateConfiguration(): List<CalendarInput>{
    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)
    current = CurrentDate(month,year)
    return createCalendarList(month,year)
}
// current selected date + 1 month
fun nextCurrentDateConfiguration(): List<CalendarInput>{
    current = if(isTheLastMonthOfYear()){
        CurrentDate(1,current.year+1)
    }else{
        CurrentDate(current.month+1,current.year)
    }
    return createCalendarList(current.month,current.year)
}

fun isTheLastMonthOfYear(): Boolean{
    if(current.month==12){
        return true
    }
    return false
}

fun isTheFistMonthOfYear(): Boolean{
    if(current.month==1){
        return true
    }
    return false
}

fun previousCurrentDateConfiguration(): List<CalendarInput>{
    current = if(isTheFistMonthOfYear()){
        CurrentDate(12,current.year-1)
    }else{
        CurrentDate(current.month-1,current.year)
    }
    return createCalendarList(current.month,current.year)
}

fun getNumberDaysByMonth(month : Int, year : Int): Int{
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.YEAR, year)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun createCalendarList(month : Int, year : Int): List<CalendarInput> {
    val calendarInputs = mutableListOf<CalendarInput>()
    // calendar
    val calendar = Calendar.getInstance()
    val numberOfDaysInMonth = getNumberDaysByMonth(month, year)
    calendar[Calendar.YEAR] = year
    calendar[Calendar.MONTH] = month
    calendar[Calendar.DAY_OF_MONTH] = 1
    // 1..7
    val firstDayInMonthUS = calendar[Calendar.DAY_OF_WEEK] - 1
    val firstDayInMonth = mapFrenchCalendar[firstDayInMonthUS]

    // end data from api

    for (j in 1 until firstDayInMonth){
        calendarInputs.add(CalendarInput(Day(0,0,0, listOf(""))))
    }
    for (i in 1..numberOfDaysInMonth) {
        calendarInputs.add(
            CalendarInput(
                Day(year,month,i, listOf("")),
            )
        )
    }
    return calendarInputs
}

class Day( val year: Int, val month : Int,val day : Int,val hours: List<String>){
    override fun equals(other: Any?): Boolean {
        //return super.equals(other)
        val day = other as Day
        return day.day==this.day && day.month==this.month && day.year==this.year
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }
}
data class CalendarInput(
    val day : Day,
)

/*
TODO detect number of row
 */
private const val CALENDAR_ROWS = 5
private const val CALENDAR_COLUMNS = 7