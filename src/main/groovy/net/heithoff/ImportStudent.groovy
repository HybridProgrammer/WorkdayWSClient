package net.heithoff

import workday.com.bsvc.AcademicCurricularDivisionObjectIDType
import workday.com.bsvc.AcademicCurricularDivisionSubeditDataType
import workday.com.bsvc.AcademicLevelObjectIDType
import workday.com.bsvc.AcademicPeriodSubeditDataType
import workday.com.bsvc.AdmissionsCounselorObjectIDType
import workday.com.bsvc.AdmissionsCounselorObjectType
import workday.com.bsvc.EducationalInterestObjectIDType
import workday.com.bsvc.EducationalInterestObjectType
import workday.com.bsvc.ImportStudentApplicationsRequestType
import workday.com.bsvc.ImportStudentPersonalInformationRequestType
import workday.com.bsvc.ImportStudentRecruitingRatingAssignmentRequestType
import workday.com.bsvc.ImportStudentRecruitmentRequestType
import workday.com.bsvc.LocationObjectIDType
import workday.com.bsvc.LocationObjectType
import workday.com.bsvc.RegionObjectType
import workday.com.bsvc.SourceDetailObjectIDType
import workday.com.bsvc.SourceDetailObjectType
import workday.com.bsvc.StudentApplicationHVType
import workday.com.bsvc.StudentApplicationObjectIDType
import workday.com.bsvc.StudentApplicationObjectType
import workday.com.bsvc.StudentPersonalInformationDataType
import workday.com.bsvc.StudentPersonalInformationType
import workday.com.bsvc.StudentPreliminaryAwardSubeditDataType
import workday.com.bsvc.StudentProspectSourceObjectType
import workday.com.bsvc.StudentProspectStageObjectIDType
import workday.com.bsvc.StudentProspectStageObjectType
import workday.com.bsvc.StudentProspectTypeObjectIDType
import workday.com.bsvc.StudentProspectTypeObjectType
import workday.com.bsvc.StudentRecruitingReferralSourceObjectIDType
import workday.com.bsvc.StudentRecruitingReferralSourceObjectType
import workday.com.bsvc.StudentRecruitmentDataW25Type
import workday.com.bsvc.StudentRecruitmentHVType
import workday.com.bsvc.StudentRecruitmentProspectDataType
import workday.com.bsvc.StudentTagObjectIDType
import workday.com.bsvc.StudentTagObjectType

import javax.xml.datatype.Duration
import javax.xml.datatype.XMLGregorianCalendar

/**
 * Copyright 2013 Jason Heithoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * User: jason
 * Date: 2/25/19
 * Time: 8:25 PM
 *
 */
class ImportStudent {
    boolean importStudentRequest() {
        ImportStudentRecruitmentRequestType request = new ImportStudentRecruitmentRequestType()
        request.ID = "jh1"
        StudentRecruitmentHVType student = new StudentRecruitmentHVType()
        student.studentRecruitmentData = new StudentRecruitmentDataW25Type()
        AcademicCurricularDivisionSubeditDataType academicCurricularDivisionSubeditDataType = new AcademicCurricularDivisionSubeditDataType()
        academicCurricularDivisionSubeditDataType.academicUnitReference.ID.add(new AcademicCurricularDivisionObjectIDType(type: "type", value: "value"))
        academicCurricularDivisionSubeditDataType.academicLevelReference.ID.add(new AcademicLevelObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.academicUnitSubeditData.add(academicCurricularDivisionSubeditDataType)

        AcademicPeriodSubeditDataType academicPeriodSubeditDataType = new AcademicPeriodSubeditDataType()
        academicPeriodSubeditDataType.academicPeriodStartDate = XMLGregorianCalendar.newInstance()
        academicPeriodSubeditDataType.academicPeriodEndDate = XMLGregorianCalendar.newInstance()
        student.studentRecruitmentData.academicPeriodSubeditData.add(academicPeriodSubeditDataType)

        StudentProspectStageObjectType studentProspectStageObjectType = new StudentProspectStageObjectType()
        studentProspectStageObjectType.ID.add(new StudentProspectStageObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.studentProspectStageReference = studentProspectStageObjectType

        EducationalInterestObjectType educationalInterestObjectType = new EducationalInterestObjectType()
        educationalInterestObjectType.ID.add(new EducationalInterestObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.educationalInterestReference.add(educationalInterestObjectType)

        StudentProspectSourceObjectType studentProspectSourceObjectType = new StudentProspectSourceObjectType()
        studentProspectSourceObjectType.ID.add(new StudentProspectStageObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.studentProspectSourceReference = studentProspectSourceObjectType

        SourceDetailObjectType sourceDetailObjectType = new SourceDetailObjectType()
        sourceDetailObjectType.ID.add(new SourceDetailObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.sourceDetailReference = sourceDetailObjectType

        StudentRecruitingReferralSourceObjectType studentRecruitingReferralSourceObjectType = new StudentRecruitingReferralSourceObjectType()
        studentRecruitingReferralSourceObjectType.ID.add(new StudentRecruitingReferralSourceObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.studentRecruitingReferralSourceReference.add(studentRecruitingReferralSourceObjectType)

        RegionObjectType region = new RegionObjectType()
        student.studentRecruitmentData.regionReference.add(region)

        RegionObjectType regionToExclude = new RegionObjectType()
        student.studentRecruitmentData.regionToExcludeReference.add(regionToExclude)

        AdmissionsCounselorObjectType admissionsCounselor = new AdmissionsCounselorObjectType()
        admissionsCounselor.ID.add(new AdmissionsCounselorObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.admissionsCounselorReference.add(admissionsCounselor)


        AdmissionsCounselorObjectType admissionsCounselorToExclude = new AdmissionsCounselorObjectType()
        admissionsCounselorToExclude.ID.add(new AdmissionsCounselorObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.admissionsCounselorToExcludeReference.add(admissionsCounselorToExclude)

        StudentProspectTypeObjectType studentProspectTypeObjectType  = new StudentProspectTypeObjectType()
        studentProspectTypeObjectType.ID.add(new StudentProspectTypeObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.studentProspectTypeReference.add(studentProspectTypeObjectType)


        StudentTagObjectType studentTagObjectType = new StudentTagObjectType()
        studentTagObjectType.ID.add(new StudentTagObjectIDType(type: "type", value: "value"))
        student.studentRecruitmentData.studentTagsReference.add(studentTagObjectType)

        student.studentRecruitmentData.locationReference = new LocationObjectType()
        student.studentRecruitmentData.locationReference.ID.add(new LocationObjectIDType(type: "type", value: "value"))
        StudentApplicationObjectType   studentApplicationObjectType = new StudentApplicationObjectType()
        studentApplicationObjectType.ID.add(new StudentApplicationObjectIDType())
        student.studentRecruitmentData.sourceStudentApplicationReference = studentApplicationObjectType

        StudentPreliminaryAwardSubeditDataType studentPreliminaryAwardSubeditDataType = new StudentPreliminaryAwardSubeditDataType()
        StudentRecruitmentProspectDataType studentRecruitmentProspectDataType = new StudentRecruitmentProspectDataType()
        student.studentRecruitmentData = studentRecruitmentProspectDataType


        XMLGregorianCalendar expectedAvailability

        student.studentRecruitmentData.expectedAvailability = expectedAvailability
        request.studentRecruitment.add(student)
    }

    def Import_Student_Applications() {
        ImportStudentApplicationsRequestType request = new ImportStudentRecruitmentRequestType()
        StudentApplicationHVType student = new StudentApplicationHVType()
        student.studentApplicationData = new StudentRecruitmentDataW25Type()
        student.studentApplicationData.studentProspectStageReference = new StudentProspectStageObjectType()
    }

    def Import_Student_Personal_Information() {
        ImportStudentPersonalInformationRequestType request = new ImportStudentPersonalInformationRequestType()
        StudentPersonalInformationType studentPersonalInformation = new StudentPersonalInformationType()
        StudentPersonalInformationDataType studentPersonalInformationDataType = new StudentPersonalInformationDataType()
        studentPersonalInformationDataType.studentID
        studentPersonalInformationDataType.studentPersonData.contactInformationData
        studentPersonalInformationDataType.studentPersonData.personNameData
        studentPersonalInformation.studentData.add(studentPersonalInformationDataType)
        request.student.add()
    }
}
