package skills.services.events

import callStack.profiler.Profile
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import skills.controller.exceptions.SkillException
import skills.services.events.pointsAndAchievements.PointsAndAchievementsHandler
import skills.storage.model.SkillDef
import skills.storage.model.SkillRelDef
import skills.storage.model.UserAchievement
import skills.storage.model.UserPerformedSkill
import skills.storage.repos.SkillEventsSupportRepo
import skills.storage.repos.SkillRelDefRepo
import skills.storage.repos.UserAchievedLevelRepo
import skills.storage.repos.UserPerformedSkillRepo

@Service
@CompileStatic
@Slf4j
class SkillEventsService {

    @Autowired
    UserPerformedSkillRepo performedSkillRepository

    @Autowired
    SkillEventsSupportRepo skillEventsSupportRepo

    @Autowired
    UserAchievedLevelRepo achievedLevelRepo

    @Autowired
    TimeWindowHelper timeWindowHelper

    @Autowired
    CheckDependenciesHelper checkDependenciesHelper

    @Autowired
    CheckRecommendationHelper checkRecommendationHelper

    @Autowired
    PointsAndAchievementsHandler pointsAndAchievementsHandler

    @Autowired
    AchievedBadgeHandler achievedBadgeHandler

    @Transactional
    @Profile
    SkillEventResult reportSkill(String projectId, String skillId, String userId, Date incomingSkillDate = new Date()) {
        assert projectId
        assert skillId

        // TODO: make a builder for the class
        SkillEventResult res = new SkillEventResult()

        SkillEventsSupportRepo.SkillDefMin skillDefinition = getSkillDef(projectId, skillId)

        Long numExistingSkills = getNumExistingSkills(userId, projectId, skillId)
        numExistingSkills = numExistingSkills ?: 0 // account for null

        if (hasReachedMaxPoints(numExistingSkills, skillDefinition)) {
            res.skillApplied = false
            res.explanation = "This skill reached its maximum points"
            return res
        }

        TimeWindowHelper.TimeWindowRes timeWindowRes = timeWindowHelper.checkTimeWindow(skillDefinition, userId, incomingSkillDate)
        if (timeWindowRes.isFull()) {
            res.skillApplied = false
            res.explanation = timeWindowRes.msg
            return res
        }

        CheckDependenciesHelper.DependencyCheckRes dependencyCheckRes = checkDependenciesHelper.check(userId, projectId, skillId)
        if (dependencyCheckRes.hasNotAchievedDependents) {
            res.skillApplied = false
            res.explanation = dependencyCheckRes.msg
            return res
        }

        UserPerformedSkill performedSkill = new UserPerformedSkill(userId: userId, skillId: skillId, projectId: projectId, performedOn: incomingSkillDate, skillRefId: skillDefinition.id)
        savePerformedSkill(performedSkill)

        List<CompletionItem> achievements = pointsAndAchievementsHandler.updatePointsAndAchievements(userId, skillDefinition, incomingSkillDate)
        if (achievements) {
            res.completed.addAll(achievements)
        }

        boolean requestedSkillCompleted = hasReachedMaxPoints(numExistingSkills + 1, skillDefinition)
        if (requestedSkillCompleted) {
            documentSkillAchieved(userId, numExistingSkills, skillDefinition, res)
            achievedBadgeHandler.checkForBadges(res, userId, skillDefinition)
        }

        return res
    }

    @Profile
    private void savePerformedSkill(UserPerformedSkill performedSkill) {
        performedSkillRepository.save(performedSkill)
        log.debug("Saved skill [{}]", performedSkill)
    }

    @Profile
    private long getNumExistingSkills(String userId, String projectId, String skillId) {
        performedSkillRepository.countByUserIdAndProjectIdAndSkillId(userId, projectId, skillId)
    }

    @Profile
    private SkillEventsSupportRepo.SkillDefMin getSkillDef(String projectId, String skillId) {
        SkillEventsSupportRepo.SkillDefMin skillDefinition = skillEventsSupportRepo.findByProjectIdAndSkillIdAndType(projectId, skillId, SkillDef.ContainerType.Skill)
        if (!skillDefinition) {
            throw new SkillException("Skill definition does not exist. Must create the skill definition first!", projectId, skillId)
        }
        return skillDefinition
    }

    @Profile
    private void documentSkillAchieved(String userId, long numExistingSkills, SkillEventsSupportRepo.SkillDefMin skillDefinition, SkillEventResult res) {
        UserAchievement skillAchieved = new UserAchievement(userId: userId, projectId: skillDefinition.projectId, skillId: skillDefinition.skillId, skillRefId: skillDefinition?.id,
                pointsWhenAchieved: ((numExistingSkills.intValue() + 1) * skillDefinition.pointIncrement))
        achievedLevelRepo.save(skillAchieved)

        List<RecommendationItem> recommendationItems = checkRecommendationHelper.checkForRecommendations(userId, skillDefinition.projectId, skillDefinition.skillId)
        // only return first 10
        recommendationItems = recommendationItems?.take(10)
        res.completed.add(new CompletionItem(type: CompletionItem.CompletionItemType.Skill, id: skillDefinition.skillId, name: skillDefinition.name, recommendations: recommendationItems))
    }

    private boolean hasReachedMaxPoints(long numSkills, SkillEventsSupportRepo.SkillDefMin skillDefinition) {
        return numSkills * skillDefinition.pointIncrement >= skillDefinition.totalPoints
    }

}