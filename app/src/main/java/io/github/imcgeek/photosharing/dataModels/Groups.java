package io.github.imcgeek.photosharing.dataModels;

/**
 * Created by imcgeek on 24/2/18.
 */

public class Groups {
    private String GroupId;
    private String GroupName;
    private String GroupDescription;
    private String GroupCreatedDate;
    private String GroupCreatedBy;

    public Groups() {
    }

    public Groups(String groupId, String groupName, String groupDescription, String groupCreatedDate, String groupCreatedBy) {
        GroupId = groupId;
        GroupName = groupName;
        GroupDescription = groupDescription;
        GroupCreatedDate = groupCreatedDate;
        GroupCreatedBy = groupCreatedBy;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupDescription() {
        return GroupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        GroupDescription = groupDescription;
    }

    public String getGroupCreatedDate() {
        return GroupCreatedDate;
    }

    public void setGroupCreatedDate(String groupCreatedDate) {
        GroupCreatedDate = groupCreatedDate;
    }

    public String getGroupCreatedBy() {
        return GroupCreatedBy;
    }

    public void setGroupCreatedBy(String groupCreatedBy) {
        GroupCreatedBy = groupCreatedBy;
    }
}
