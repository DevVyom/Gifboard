package com.vyom.moderngifboard.provider
import com.vyom.moderngifboard.model.GifItem
data class GifPage(val items:List<GifItem>,val next:String?=null)
interface GifProvider { suspend fun search(query:String,cursor:String?=null):GifPage; suspend fun trending(cursor:String?=null):GifPage }