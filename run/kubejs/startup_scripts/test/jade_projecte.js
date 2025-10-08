StartupEvents.postInit(event => {
  let $WailaClientRegistration
  if (Platform.isClientEnvironment()) {
    $WailaClientRegistration = Java.loadClass("snownee.jade.impl.WailaClientRegistration")
    $WailaClientRegistration.instance().addTooltipCollectedCallback(0, (tooltip, accessor) => {
      global.jadeCallback(tooltip, accessor)
    })
    $WailaClientRegistration.instance().tooltipCollectedCallback.sort()
  }
})

let $Integer = Java.loadClass("java.lang.Integer")
let $ElementHelper = Java.loadClass("snownee.jade.impl.ui.ElementHelper") 
let $NumberFormat = Java.loadClass("java.text.NumberFormat")
let $Util = Java.loadClass("net.minecraft.Util")

let PROJECTE_FORMATTER = $Util.make($NumberFormat.getInstance(), formatter => formatter.setMaximumFractionDigits(1))
let CURRENCY_FORMATTER = $NumberFormat.getCurrencyInstance()
// let someInvalidSignature = Java.loadClass("net.minecraft.Util", "SomeInvalidSignature")

global.jadeCallback = (tooltip, accessor) => {
  if (!accessor.hitResult) return
  
  tooltip.getTooltip()["replace(net.minecraft.resources.ResourceLocation,java.util.function.UnaryOperator)"]("projecte:emc_provider", (listOfLines) => {
    for (let lines of listOfLines) {
      let idx = lines.findIndex((line) => line.text?.contents?.key == "emc.projecte.tooltip")
      if (idx == -1) return listOfLines

      let text = lines.get(idx).text
      let textStyle = text.getStyle()

      let valueComponent = text.contents.args[0]
      let valueStyle = valueComponent.getStyle()
      let valueText = valueComponent.getContents().text()

      let newValue = CURRENCY_FORMATTER.format((PROJECTE_FORMATTER.parse(valueText) / 100))
      let newValueComponent = Text.of(newValue)["withStyle(net.minecraft.network.chat.Style)"](valueStyle)

      let newTextComponent = Text.translate("emc.projecte.tooltip", newValueComponent)["withStyle(net.minecraft.network.chat.Style)"](textStyle)

      lines.set(idx, $ElementHelper.INSTANCE.text(newTextComponent))
    }
    return listOfLines
  })
}

