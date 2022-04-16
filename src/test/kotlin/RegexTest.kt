fun main(){
    val regex = Regex("(?<=#group\\.mute(\\\\)?:)\\d+")
    println(regex.find("#group.mute:12345")?.value?.toLong())
}