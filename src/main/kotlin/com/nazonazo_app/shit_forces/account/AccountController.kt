package com.nazonazo_app.shit_forces.account

import com.nazonazo_app.shit_forces.EmptyResponse
import com.nazonazo_app.shit_forces.session.SharedSessionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

const val ACCOUNT_RANKING_ONE_PAGE = 20
@CrossOrigin(origins = ["http://localhost:3000"], allowCredentials = "true")
@RestController
class AccountController(
    private val accountService: AccountService,
    private val sharedAccountService: SharedAccountService,
    private val sharedSessionService: SharedSessionService
) {

    @RequestMapping(
        "api/signup",
        headers = ["Content-Type=application/json"],
        method = [RequestMethod.POST]
    )
    fun createAccountResponse(
        @RequestBody requestAccount: RequestAccountForCertification,
        httpServletResponse: HttpServletResponse
    ): AccountInfo {
        val account = accountService.createAccount(requestAccount)
        sharedSessionService.createNewSession(account.name, httpServletResponse)
        return account
    }

    @PostMapping(
        "api/login",
        headers = ["Content-Type=application/json"]
    )
    fun loginAccountResponse(
        @RequestBody requestAccount: RequestAccountForCertification,
        httpServletResponse: HttpServletResponse
    ): EmptyResponse {
        accountService.loginAccount(requestAccount, httpServletResponse)
        return EmptyResponse()
    }

    @GetMapping("api/account/{accountName}")
    fun getAccountByNameResponse(@PathVariable("accountName") accountName: String): ResponseAccountInfo {
        val account = sharedAccountService.getAccountByName(accountName)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return ResponseAccountInfoInterface.build(account)
    }

    @PutMapping("api/account/{accountName}/name")
    fun changeAccountNameResponse(
        @PathVariable("accountName") accountName: String,
        @RequestBody requestAccount: RequestAccountForCertification,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ) {
        val sessionAccountName = sharedSessionService.getSessionAccountName(httpServletRequest)
        if (sessionAccountName != accountName) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        accountService.changeAccountName(accountName, requestAccount, httpServletRequest, httpServletResponse)
    }

    @GetMapping("api/ranking")
    fun getAccountsRankingResponse(@RequestParam("page") page: Int): ResponseAccountRanking {
        return accountService.getAccountRanking(page)
    }

    @GetMapping("api/account/{accountName}/history")
    fun getAccountContestResultHistoryResponse(
        @PathVariable accountName: String
    ): List<AccountRatingChangeHistory> {
        return accountService.getAccountContestResultHistory(accountName)
    }
}
