package com.subsensex.advanced

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random

private val BG = Color(0xFFF3F6F8)
private val INK = Color(0xFF13232E)
private val BLUE = Color(0xFF1769AA)
private val GREEN = Color(0xFF16835A)
private val YELLOW = Color(0xFFF2B400)
private val ORANGE = Color(0xFFEF6C00)
private val RED = Color(0xFFD93025)
private val PURPLE = Color(0xFF7A4DB4)
private val GREY = Color(0xFF71828D)

private enum class Scenario(val label: String) {
    NORMAL("Live random feed"), LOCAL("Local deformation"), PROGRESSIVE("Progressive subsidence"), SENSOR("Sensor failure")
}
private enum class Risk(val label: String) { STABLE("STABLE"), WATCH("WATCH"), DEVELOPING("DEVELOPING"), HIGH("HIGH"), CRITICAL("CRITICAL") }
private data class Node(val id:String,val row:Int,val col:Int,val dev:Double,val vel:Double,val crack:Double,val vib:Double,val battery:Int,val rssi:Int,val suspect:Boolean,val coherent:Boolean)
private data class Event(val time:Long,val severity:String,val title:String,val detail:String,val nodes:String,val ack:Boolean=false)
private data class Snapshot(val nodes:List<Node>,val risk:Risk,val score:Int,val coherence:Double,val pattern:String,val confidence:Int,val alerts:List<Event>,val tick:Int,val unsynced:Int,val online:Boolean,val scenario:Scenario,val avgBattery:Int,val temperature:Double,val humidity:Double,val packets:Int,val maxVelocity:Double,val maxVibration:Double,val maxDeviation:Double,val history:List<Int>)

private class MineEngine {
    private var scenario=Scenario.NORMAL
    private var tick=0
    private val random=Random(42)
    private var lastRisk=Risk.STABLE
    private var lastAlertTick=-20
    private val events=mutableListOf<Event>()
    private val scoreHistory=ArrayDeque<Int>()

    fun setScenario(s:Scenario){ scenario=s; tick=0; lastRisk=Risk.STABLE; events.clear(); scoreHistory.clear() }

    fun step(online:Boolean):Snapshot {
        tick++
        val centerR=1.5
        val centerC=2.5
        val nodes=(0 until 25).map { i ->
            val r=i/5
            val c=i%5
            val d=sqrt((r-centerR).pow(2)+(c-centerC).pow(2))
            val w=exp(-(d*d)/2.2)
            val growth=when(scenario) {
                Scenario.LOCAL -> 0.45*tick
                Scenario.PROGRESSIVE -> 0.20*tick+0.012*tick*tick
                else -> 0.0
            }
            var dev=growth*w+random.nextDouble(-1.15,1.15)
            var crack=max(0.0,(growth-8)*0.025*w+random.nextDouble(-0.02,0.02))
            var vib=0.05+random.nextDouble(0.0,0.07)+if(scenario==Scenario.PROGRESSIVE) 0.18*w*min(1.0,tick/45.0) else 0.0
            var suspect=false
            if(scenario==Scenario.SENSOR && i==12 && tick>4) {
                dev=60+random.nextDouble(0.0,30.0)
                crack=0.2
                vib=1.4
                suspect=true
            }
            val vel=when(scenario) {
                Scenario.PROGRESSIVE -> 0.20+0.024*tick
                Scenario.LOCAL -> 0.35+random.nextDouble(0.0,0.18)
                else -> random.nextDouble(0.01,0.12)
            }
            val coherent=abs(dev)>1.5 && !suspect && scenario!=Scenario.NORMAL
            Node("N%02d".format(i+1),r,c,dev,vel,crack,vib,(96-(tick/80)).coerceAtLeast(45),-62-random.nextInt(0,25),suspect,coherent)
        }
        val coherent=nodes.count { it.coherent }
        val suspects=nodes.count { it.suspect }
        val maxDev=nodes.maxOf { abs(it.dev) }
        val maxVel=nodes.filter { it.coherent }.maxOfOrNull { it.vel } ?: 0.0
        val maxCrack=nodes.maxOf { it.crack }
        val maxVib=nodes.maxOf { it.vib }
        val score=round(
            25*(maxDev/20).coerceIn(0.0,1.0)+
            20*(maxVel/1.0).coerceIn(0.0,1.0)+
            20*(coherent/12.0).coerceIn(0.0,1.0)+
            15*(maxCrack/2).coerceIn(0.0,1.0)+
            10*((maxVib-0.07)/0.43).coerceIn(0.0,1.0)
        ).toInt().coerceIn(0,100)
        val risk=when {
            score<=20 -> Risk.STABLE
            score<=40 -> Risk.WATCH
            score<=60 -> Risk.DEVELOPING
            score<=80 -> Risk.HIGH
            else -> Risk.CRITICAL
        }
        val pattern=when {
            scenario==Scenario.SENSOR -> "Sensor anomaly"
            scenario==Scenario.PROGRESSIVE && coherent>5 -> "Accelerating deformation"
            coherent>4 -> "Spatial deformation"
            coherent>0 -> "Local deformation"
            else -> "Normal field"
        }
        val confidence=when {
            suspects>0 && coherent==0 -> 35
            else -> (55+coherent.coerceAtMost(12)*3).coerceAtMost(95)
        }
        if(risk.ordinal>lastRisk.ordinal && risk.ordinal>=Risk.WATCH.ordinal && tick-lastAlertTick>3) {
            events.add(0,Event(System.currentTimeMillis(),risk.label,"Ground risk raised to ${risk.label}","Score $score/100 • $pattern",nodes.filter{it.coherent}.take(10).joinToString(","){it.id}))
            lastAlertTick=tick
        }
        if(suspects>0 && tick%6==0) {
            events.add(0,Event(System.currentTimeMillis(),"MEDIUM","Sensor integrity warning","N13 disagrees strongly with its neighbours and is excluded from ground risk.","N13"))
        }
        lastRisk=risk
        scoreHistory.addLast(score)
        while(scoreHistory.size>30) scoreHistory.removeFirst()
        val temp=27.0+sin(tick/8.0)*0.8+random.nextDouble(-0.35,0.35)
        val humidity=(58.0+sin(tick/11.0)*4.0+random.nextDouble(-1.5,1.5)).coerceIn(35.0,85.0)
        val packets=25+random.nextInt(0,12)
        return Snapshot(nodes,risk,score,coherent/25.0,pattern,confidence,events.take(30),tick,events.size*25,online,scenario,nodes.map{it.battery}.average().toInt(),temp,humidity,packets,maxVel,maxVib,maxDev,scoreHistory.toList())
    }
}

private lateinit var appContext: Context

private fun postAlert(e:Event) {
    if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(appContext,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return
    val n=NotificationCompat.Builder(appContext,"subsensex_advanced")
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle(e.title)
        .setContentText(e.detail)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(appContext).notify(e.time.hashCode(),n)
}

class MainActivity:ComponentActivity() {
    private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        appContext=this
        if(Build.VERSION.SDK_INT>=26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("subsensex_advanced","SUBSENSE-X Safety Alerts",NotificationManager.IMPORTANCE_HIGH)
            )
        }
        if(Build.VERSION.SDK_INT>=33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent { App() }
    }
}

@Composable private fun App() {
    val engine=remember { MineEngine() }
    var scenario by remember { mutableStateOf(Scenario.NORMAL) }
    var snap by remember { mutableStateOf<Snapshot?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var lastPosted by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }
    val online=rememberNetwork()

    LaunchedEffect(scenario,online,running) {
        engine.setScenario(scenario)
        lastPosted = 0
        while (isActive && running) {
            val next = withContext(Dispatchers.Default) { engine.step(online) }
            snap = next
            if (next.alerts.size > lastPosted && lastPosted > 0) {
                next.alerts.firstOrNull()?.let { postAlert(it) }
            }
            lastPosted = next.alerts.size
            delay(1500)
        }
    }
    val s=snap
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(BG)) {
            Header(s)
            ScrollableTabRow(selectedTabIndex=tab,edgePadding=8.dp,containerColor=Color.White) {
                listOf("Overview","Live Map","Alerts","Analytics","System").forEachIndexed { i,t ->
                    Tab(selected=tab==i,onClick={tab=i},text={Text(t)})
                }
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                if(s==null) Calibration()
                else {
                    if(s.risk.ordinal>=Risk.HIGH.ordinal) Emergency(s)
                    when(tab) {
                        0 -> Overview(s,{scenario=it},{running=!running})
                        1 -> MapPage(s,selected){selected=it}
                        2 -> AlertsPage(s)
                        3 -> Analytics(s)
                        else -> SystemPage(s){scenario=it}
                    }
                }
                Text("SUBSENSE-X Advanced • local simulator • offline-first safety workflow • field calibration required",fontSize=10.sp,color=GREY)
            }
        }
    }
}

@Composable private fun rememberNetwork():Boolean {
    var online by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while(true) {
            val cm=appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            online=cm.activeNetwork?.let { cm.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }==true
            delay(2000)
        }
    }
    return online
}

@Composable private fun Header(s:Snapshot?) {
    Column(Modifier.background(Color.White).fillMaxWidth().padding(14.dp)) {
        Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically) {
            Column {
                Text("SUBSENSE-X",fontSize=24.sp,fontWeight=FontWeight.ExtraBold,color=INK)
                Text("MINE SAFETY COMMAND CENTER",fontSize=10.sp,color=BLUE,fontWeight=FontWeight.Bold)
            }
            Pill(if(s?.online==true)"ONLINE" else "OFFLINE",if(s?.online==true)GREEN else RED)
        }
        Row(Modifier.fillMaxWidth().padding(top=8.dp),Arrangement.spacedBy(6.dp)) {
            Metric("NODES","25", Modifier.weight(1f))
            Metric("RISK",s?.score?.toString() ?: "--", Modifier.weight(1f))
            Metric("BAT",s?.avgBattery?.let{"$it%"} ?: "--", Modifier.weight(1f))
            Metric("TICK",s?.tick?.toString() ?: "--", Modifier.weight(1f))
        }
    }
}

@Composable private fun Metric(a:String,b:String, modifier: Modifier = Modifier) {
    Column(modifier.background(BG,RoundedCornerShape(8.dp)).padding(7.dp)) {
        Text(a,fontSize=8.sp,color=GREY,fontWeight=FontWeight.Bold)
        Text(b,fontSize=12.sp,fontWeight=FontWeight.ExtraBold,color=INK)
    }
}

@Composable private fun Pill(t:String,c:Color) {
    Box(Modifier.background(c.copy(alpha=.12f),RoundedCornerShape(50)).padding(horizontal=10.dp,vertical=6.dp)) {
        Text("● $t",color=c,fontWeight=FontWeight.Bold,fontSize=11.sp)
    }
}

@Composable private fun Calibration() {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color(0xFFE8F1FA))) {
        Column(Modifier.padding(14.dp)) {
            Text("INITIALIZING LOCAL SAFETY ENGINE",fontWeight=FontWeight.Bold,color=BLUE)
            Text("Building the baseline and sensor field. All processing stays on-device.",fontSize=12.sp)
        }
    }
}

@Composable private fun Emergency(s:Snapshot) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color(0xFFFFE6E3))) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text("LOCAL SAFETY ALERT • ${s.risk.label}",color=RED,fontSize=16.sp,fontWeight=FontWeight.ExtraBold)
            Text("${s.pattern} detected with score ${s.score}/100.",fontSize=12.sp)
            Text("Verify the affected area and follow mine safety protocol.",fontSize=12.sp,fontWeight=FontWeight.Bold)
            Text("Works without Internet.",fontSize=11.sp,color=BLUE)
        }
    }
}

@Composable private fun Overview(s:Snapshot,onScenario:(Scenario)->Unit,onToggle:()->Unit) {
    LiveStatus(s,onToggle)
    RiskCard(s)
    LiveTelemetry(s)
    Kpis(s)
    LiveTrend(s)
    Evidence(s)
    ScenarioCard(s,onScenario)
    Events(s.alerts.take(3))
}

@Composable private fun LiveStatus(s:Snapshot,onToggle:()->Unit) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Row(Modifier.fillMaxWidth().padding(12.dp),Arrangement.SpaceBetween,Alignment.CenterVertically) {
            Column {
                Text("LIVE TELEMETRY STREAM",fontWeight=FontWeight.ExtraBold,fontSize=14.sp)
                Text("Simulator is generating fresh sensor packets every 1.5 seconds",fontSize=10.sp,color=GREY)
            }
            Button(onClick=onToggle,contentPadding=PaddingValues(horizontal=12.dp,vertical=6.dp),colors=ButtonDefaults.buttonColors(containerColor=if(s.tick>0) GREEN else GREY)) {
                Text(if(s.tick>0) "LIVE" else "START",fontSize=10.sp)
            }
        }
    }
}

@Composable private fun LiveTelemetry(s:Snapshot) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Text("LIVE SENSOR TELEMETRY",fontWeight=FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(6.dp)) {
                Metric("MAX DEV","%.2f mm".format(s.maxDeviation),Modifier.weight(1f))
                Metric("VELOCITY","%.3f".format(s.maxVelocity),Modifier.weight(1f))
                Metric("VIB","%.2f".format(s.maxVibration),Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(6.dp)) {
                Metric("TEMP","%.1f°C".format(s.temperature),Modifier.weight(1f))
                Metric("HUMID","%.1f%%".format(s.humidity),Modifier.weight(1f))
                Metric("PACKETS","${s.packets}/s",Modifier.weight(1f))
            }
            Text("Last packet: ${SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(Date())}  •  sequence #${s.tick}",fontSize=10.sp,color=GREY)
        }
    }
}

@Composable private fun LiveTrend(s:Snapshot) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("LIVE RISK TREND",fontWeight=FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth().height(70.dp),horizontalArrangement=Arrangement.spacedBy(2.dp),verticalAlignment=Alignment.Bottom) {
                s.history.forEach { value ->
                    Box(Modifier.weight(1f).fillMaxHeight((value.coerceIn(2,100))/100f).background(riskColor(if(value<=20)Risk.STABLE else if(value<=40)Risk.WATCH else if(value<=60)Risk.DEVELOPING else if(value<=80)Risk.HIGH else Risk.CRITICAL),RoundedCornerShape(2.dp)))
                }
            }
            Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween) {
                Text("older",fontSize=9.sp,color=GREY)
                Text("now • ${s.score}/100",fontSize=9.sp,fontWeight=FontWeight.Bold,color=riskColor(s.risk))
            }
        }
    }
}

@Composable private fun RiskCard(s:Snapshot) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically) {
                Column {
                    Text("GROUND RISK",fontSize=10.sp,color=GREY,fontWeight=FontWeight.Bold)
                    Text(s.risk.label,fontSize=30.sp,fontWeight=FontWeight.ExtraBold,color=riskColor(s.risk))
                    Text(s.pattern,fontSize=12.sp)
                }
                Box(Modifier.size(82.dp).background(riskColor(s.risk).copy(alpha=.12f),CircleShape),contentAlignment=Alignment.Center) {
                    Text(s.score.toString(),fontSize=27.sp,fontWeight=FontWeight.ExtraBold,color=riskColor(s.risk))
                }
            }
            LinearProgressIndicator(progress={s.score/100f},Modifier.fillMaxWidth().height(8.dp),color=riskColor(s.risk))
            Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween) {
                Text("Coherence ${(s.coherence*100).toInt()}%",fontSize=11.sp)
                Text("Confidence ${s.confidence}%",fontSize=11.sp,fontWeight=FontWeight.Bold)
            }
        }
    }
}

@Composable private fun Kpis(s:Snapshot) {
    Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(7.dp)) {
        K("COHERENT",s.nodes.count{it.coherent}.toString(),GREEN, Modifier.weight(1f))
        K("SUSPECT",s.nodes.count{it.suspect}.toString(),PURPLE, Modifier.weight(1f))
        K("ACTIVE",s.alerts.count{!it.ack}.toString(),RED, Modifier.weight(1f))
        K("QUEUE",s.unsynced.toString(),BLUE, Modifier.weight(1f))
    }
}

@Composable private fun K(a:String,b:String,c:Color, modifier: Modifier = Modifier) {
    Card(modifier,colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(9.dp)) {
            Text(a,fontSize=8.sp,color=GREY,fontWeight=FontWeight.Bold)
            Text(b,fontSize=18.sp,color=c,fontWeight=FontWeight.ExtraBold)
        }
    }
}

@Composable private fun Evidence(s:Snapshot) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text("DECISION EVIDENCE",fontWeight=FontWeight.ExtraBold)
            Text("• ${s.nodes.count{it.coherent}} nodes show correlated movement",fontSize=12.sp)
            Text("• Max displacement ${"%.1f".format(s.nodes.maxOf{abs(it.dev)})} mm",fontSize=12.sp)
            Text("• Pattern: ${s.pattern}",fontSize=12.sp)
            Text("• Sensor faults are excluded from ground-risk scoring",fontSize=12.sp)
        }
    }
}

@Composable private fun MapPage(s:Snapshot,selected:Int?,pick:(Int)->Unit) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
            Text("LIVE 5 × 5 SENSOR FIELD",fontSize=15.sp,fontWeight=FontWeight.ExtraBold)
            for(r in 0..4) {
                Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(5.dp)) {
                    for(c in 0..4) {
                        val i=r*5+c
                        val n=s.nodes[i]
                        val col=nodeColor(n)
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).background(col,RoundedCornerShape(8.dp))
                                .then(if(selected==i) Modifier.border(3.dp,INK,RoundedCornerShape(8.dp)) else Modifier)
                                .clickable{pick(i)},
                            contentAlignment=Alignment.Center
                        ) {
                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                Text(n.id,color=if(col==YELLOW)INK else Color.White,fontSize=10.sp,fontWeight=FontWeight.Bold)
                                Text("%.1f".format(n.dev),color=if(col==YELLOW)INK else Color.White,fontSize=9.sp)
                            }
                        }
                    }
                }
            }
            Text("GREEN stable • YELLOW watch • ORANGE developing • RED high • PURPLE sensor fault",fontSize=10.sp,color=GREY)
        }
    }
    selected?.let { NodeCard(s.nodes[it]) }
    Health(s.nodes)
}

@Composable private fun NodeCard(n:Node) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            Text("${n.id} • NODE TELEMETRY",fontWeight=FontWeight.ExtraBold)
            Text("Deviation ${"%.2f".format(n.dev)} mm • velocity ${"%.3f".format(n.vel)}",fontSize=11.sp)
            Text("Crack ${"%.2f".format(n.crack)} mm • vibration ${"%.2f".format(n.vib)}",fontSize=11.sp)
            Text("Battery ${n.battery}% • RSSI ${n.rssi} dBm",fontSize=11.sp)
            Text(if(n.suspect)"SENSOR SUSPECT — excluded from risk" else if(n.coherent)"COHERENT GROUND MOVEMENT" else "NORMAL",fontSize=11.sp,fontWeight=FontWeight.Bold,color=if(n.suspect)PURPLE else if(n.coherent)RED else GREEN)
        }
    }
}

@Composable private fun Health(nodes:List<Node>) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text("NODE HEALTH",fontWeight=FontWeight.ExtraBold)
            nodes.sortedBy{it.battery}.take(7).forEach { n ->
                Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween) {
                    Text(n.id,fontSize=11.sp,fontWeight=FontWeight.Bold)
                    Text("${n.battery}%",fontSize=11.sp,color=if(n.battery<25)ORANGE else GREEN)
                    Text("${n.rssi} dBm",fontSize=11.sp,color=GREY)
                    Text(if(n.suspect)"FAULT" else "OK",fontSize=10.sp,color=if(n.suspect)PURPLE else GREEN,fontWeight=FontWeight.Bold)
                }
            }
        }
    }
}

@Composable private fun AlertsPage(s:Snapshot) {
    Text("LOCAL INCIDENT LOG",fontSize=15.sp,fontWeight=FontWeight.ExtraBold)
    if(s.alerts.isEmpty()) Text("No alerts recorded.",fontSize=12.sp,color=GREY)
    Events(s.alerts)
}

@Composable private fun Events(events:List<Event>) {
    events.forEach { e ->
        Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween) {
                    Text(e.title,fontWeight=FontWeight.Bold,fontSize=13.sp)
                    Text(e.severity,color=severity(e.severity),fontSize=10.sp,fontWeight=FontWeight.Bold)
                }
                Text("${SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(Date(e.time))} • ${e.nodes}",fontSize=10.sp,color=GREY)
                Text(e.detail,fontSize=11.sp)
                Text(if(e.ack)"ACKNOWLEDGED" else "Action: verify and follow mine protocol",fontSize=10.sp,fontWeight=FontWeight.Bold,color=if(e.ack)GREEN else BLUE)
            }
        }
    }
}

@Composable private fun Analytics(s:Snapshot) {
    Stat("RISK SCORE",s.score.toString(),"0–100 transparent weighted score")
    Stat("COHERENCE","${(s.coherence*100).toInt()}%","Spatial agreement across the field")
    Stat("CONFIDENCE","${s.confidence}%","Engine confidence, not a probability")
    Stat("MAX DEVIATION","${"%.2f".format(s.nodes.maxOf{abs(it.dev)})} mm","Distance from simulated baseline")
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text("DECISION TRACE",fontWeight=FontWeight.ExtraBold)
            listOf("Validate telemetry","Compare neighbours","Find spatially coherent movement","Combine magnitude, velocity, crack and vibration","Escalate only when risk level changes").forEachIndexed { i,t -> Text("${i+1}. $t",fontSize=11.sp) }
        }
    }
}

@Composable private fun Stat(a:String,b:String,c:String) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Text(a,fontSize=10.sp,color=GREY,fontWeight=FontWeight.Bold)
            Text(b,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=INK)
            Text(c,fontSize=10.sp,color=GREY)
        }
    }
}

@Composable private fun SystemPage(s:Snapshot,onScenario:(Scenario)->Unit) {
    Stat("PROCESSING","ON DEVICE","Risk, alerts and simulation run locally")
    Stat("CONNECTIVITY",if(s.online)"AVAILABLE" else "OFFLINE","Core safety workflow remains active")
    Stat("LOCAL QUEUE","${s.unsynced} RECORDS","Ready for store-and-forward sync architecture")
    Stat("SOURCE","SIMULATOR / LoRa-ready","Telemetry interface can be replaced by gateway input")
    ScenarioCard(s,onScenario)
}

@Composable private fun ScenarioCard(s:Snapshot,onPick:(Scenario)->Unit) {
    Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color.White)) {
        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Text("DEMO / FIELD TEST",fontWeight=FontWeight.ExtraBold)
            Text("Live random feed is the default. Switch scenarios to demonstrate deformation progression or sensor faults.",fontSize=11.sp,color=GREY)
            Scenario.values().toList().chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(),Arrangement.spacedBy(7.dp)) {
                    pair.forEach { sc ->
                        Button(onClick={onPick(sc)},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=if(s.scenario==sc)BLUE else GREY)) {
                            Text(sc.label,fontSize=10.sp)
                        }
                    }
                }
            }
            Text("Current: ${s.scenario.label}",fontSize=11.sp,fontWeight=FontWeight.Bold,color=BLUE)
        }
    }
}

private fun riskColor(r:Risk)=when(r){Risk.STABLE->GREEN;Risk.WATCH->YELLOW;Risk.DEVELOPING->ORANGE;Risk.HIGH,Risk.CRITICAL->RED}
private fun nodeColor(n:Node)=when{n.suspect->PURPLE;abs(n.dev)<1.5->GREEN;abs(n.dev)<4->YELLOW;abs(n.dev)<10->ORANGE;else->RED}
private fun severity(s:String)=when(s){"MEDIUM"->PURPLE;"WATCH"->YELLOW;"DEVELOPING"->ORANGE;else->RED}
