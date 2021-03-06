/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.services

import org.springframework.beans.factory.annotation.Autowired
import skills.auth.UserInfo
import skills.auth.UserInfoService
import skills.intTests.utils.DefaultIntSpec
import skills.storage.model.UserAttrs

class UserAttrsServiceSpec extends DefaultIntSpec {

    @Autowired
    UserAttrsService userAttrsService

    @Autowired
    UserInfoService userInfoService;

    def "do not override existing user attributes with null values"() {
        String userId = "${UserAttrsServiceSpec.getSimpleName()}User1"
        UserInfo userInfo = new UserInfo(
                firstName: "first",
                lastName: "last",
                nickname: "nick",
                userDn: "dn",
                email: "email",
                username: userId.toLowerCase(),
                usernameForDisplay: userId
        )
        userAttrsService.saveUserAttrs(userId, userInfo)
        when:
        userAttrsService.saveUserAttrs(userId, new UserInfo(
                username: userId.toLowerCase(),
                usernameForDisplay: userId
        ))
        then:
        UserAttrs userAttrs = userAttrsService.findByUserId(userId)
        userAttrs.userId == userInfo.username.toLowerCase()
        userAttrs.userIdForDisplay == userInfo.usernameForDisplay
        userAttrs.firstName == userInfo.firstName
        userAttrs.lastName == userInfo.lastName
        userAttrs.firstName == userInfo.firstName
        userAttrs.nickname == userInfo.nickname
        userAttrs.dn == userInfo.userDn
        userAttrs.email == userInfo.email
    }

    def "do not override existing user attributes with null values - update 1 value"() {
        String userId = "${UserAttrsServiceSpec.getSimpleName()}User1"
        UserInfo userInfo = new UserInfo(
                firstName: "first",
                lastName: "last",
                nickname: "nick",
                userDn: "dn",
                email: "email",
                username: userId.toLowerCase(),
                usernameForDisplay: userId
        )
        userAttrsService.saveUserAttrs(userId, userInfo)
        when:
        userAttrsService.saveUserAttrs(userId, new UserInfo(
                username: userId.toLowerCase(),
                usernameForDisplay: userId,
                lastName: "lastNew"
        ))
        then:
        UserAttrs userAttrs = userAttrsService.findByUserId(userId)
        userAttrs.userId == userInfo.username.toLowerCase()
        userAttrs.userIdForDisplay == userInfo.usernameForDisplay
        userAttrs.firstName == userInfo.firstName
        userAttrs.lastName == "lastNew"
        userAttrs.firstName == userInfo.firstName
        userAttrs.nickname == userInfo.nickname
        userAttrs.dn == userInfo.userDn
        userAttrs.email == userInfo.email
    }

    def "do not override existing user attributes with null values - update all value"() {
        String userId = "${UserAttrsServiceSpec.getSimpleName()}User1"
        UserInfo userInfo = new UserInfo(
                firstName: "first",
                lastName: "last",
                nickname: "nick",
                userDn: "dn",
                email: "email",
                username: userId.toLowerCase(),
                usernameForDisplay: userId
        )
        userAttrsService.saveUserAttrs(userId, userInfo)
        when:
        userAttrsService.saveUserAttrs(userId, new UserInfo(
                username: userId.toLowerCase(),
                usernameForDisplay: userId,
                firstName: "firstNew",
                lastName: "lastNew",
                nickname: "nickNew",
                userDn: "dnNew",
                email: "emailNew",
        ))
        then:
        UserAttrs userAttrs = userAttrsService.findByUserId(userId)
        userAttrs.userId == userInfo.username.toLowerCase()
        userAttrs.userIdForDisplay == userInfo.usernameForDisplay
        userAttrs.firstName == "firstNew"
        userAttrs.lastName == "lastNew"
        userAttrs.nickname == "nickNew"
        userAttrs.dn == "dnNew"
        userAttrs.email == "emailNew"
    }

    def "userInfoService.getUserName() does not override userNameForDisplay"() {
        String userId = "${UserAttrsServiceSpec.getSimpleName()}User1"
        UserInfo userInfo = new UserInfo(
                firstName: "Fake",
                lastName: "Fake",
                nickname: "Fake",
                userDn: "UserAttrsServiceSpecUser1",
                email: "fake@fakeplace",
                username: userId.toLowerCase(),
                usernameForDisplay: "${userId} for display"
        )
        userAttrsService.saveUserAttrs(userId, userInfo)

        when:
        String res = userInfoService.getUserName(userId)

        then:
        res
        res == userInfo.username.toLowerCase()
        UserAttrs userAttrs = userAttrsService.findByUserId(userId)
        userAttrs.userId == userInfo.username.toLowerCase()
        userAttrs.userIdForDisplay == userInfo.usernameForDisplay
        userAttrs.firstName == userInfo.firstName
        userAttrs.lastName == userInfo.lastName
        userAttrs.firstName == userInfo.firstName
        userAttrs.nickname == userInfo.nickname
        userAttrs.dn == userInfo.userDn
        userAttrs.email == userInfo.email
    }

    def "userNameForDisplay will use userId if not in userInfo"() {
        String userId = "${UserAttrsServiceSpec.getSimpleName()}User1"
        UserInfo userInfo = new UserInfo(
                firstName: "first",
                lastName: "last",
                nickname: "nick",
                userDn: "dn",
                email: "email",
                username: userId.toLowerCase(),
        )

        when:
        userAttrsService.saveUserAttrs(userId, userInfo)

        then:
        !userInfo.usernameForDisplay
        UserAttrs userAttrs = userAttrsService.findByUserId(userId)
        userAttrs.userId == userInfo.username.toLowerCase()
        userAttrs.userIdForDisplay == userId
        userAttrs.firstName == userInfo.firstName
        userAttrs.lastName == userInfo.lastName
        userAttrs.firstName == userInfo.firstName
        userAttrs.nickname == userInfo.nickname
        userAttrs.dn == userInfo.userDn
        userAttrs.email == userInfo.email
    }

    def "userNameForDisplay will use userNameForDisplay if in userInfo"() {
        String userId = "${UserAttrsServiceSpec.getSimpleName()}User1"
        UserInfo userInfo = new UserInfo(
                firstName: "first",
                lastName: "last",
                nickname: "nick",
                userDn: "dn",
                email: "email",
                username: userId.toLowerCase(),
                usernameForDisplay: "${userId}-Display"
        )

        when:
        userAttrsService.saveUserAttrs(userId, userInfo)

        then:
        userInfo.usernameForDisplay
        UserAttrs userAttrs = userAttrsService.findByUserId(userId)
        userAttrs.userId == userInfo.username.toLowerCase()
        userAttrs.userIdForDisplay == userInfo.usernameForDisplay
        userAttrs.firstName == userInfo.firstName
        userAttrs.lastName == userInfo.lastName
        userAttrs.firstName == userInfo.firstName
        userAttrs.nickname == userInfo.nickname
        userAttrs.dn == userInfo.userDn
        userAttrs.email == userInfo.email
    }
}
