package ba.grbo.currencyconverter.data.models.shared

interface Exchangeable : Unexchangeable {
    var exchangeRate: Evaluable
}