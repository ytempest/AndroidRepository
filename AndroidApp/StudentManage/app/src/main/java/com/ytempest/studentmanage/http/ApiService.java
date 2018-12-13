package com.ytempest.studentmanage.http;

import com.ytempest.studentmanage.model.ClassInfoResult;
import com.ytempest.studentmanage.model.CommonResult;
import com.ytempest.studentmanage.model.CourseInfoResult;
import com.ytempest.studentmanage.model.CourseListResult;
import com.ytempest.studentmanage.model.DepartmentInfoResult;
import com.ytempest.studentmanage.model.MajorInfoResult;
import com.ytempest.studentmanage.model.ManageClassListResult;
import com.ytempest.studentmanage.model.ManageDepartmentListResult;
import com.ytempest.studentmanage.model.ManageIdResult;
import com.ytempest.studentmanage.model.ManageMajorListResult;
import com.ytempest.studentmanage.model.ManageStudentListResult;
import com.ytempest.studentmanage.model.ManageTeacherListResult;
import com.ytempest.studentmanage.model.StudentInfoResult;
import com.ytempest.studentmanage.model.StudentListResult;
import com.ytempest.studentmanage.model.TeacherInfoResult;

import java.io.InputStream;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author ytempest
 *         Description：
 */
public interface ApiService {

    /**
     * 用户登录
     */
    @FormUrlEncoded
    @POST("shiro/login")
    Call<CommonResult> login(
            @Field("id") String id,
            @Field("password") String password,
            @Field("userType") String userType);


    //--------------   学生用户   --------------

    /**
     * 根据学号获取该学生的所有课程
     */
    @GET("student/getChildCourseByStudentId")
    Call<CourseListResult> getChildCourseByStudentId(
            @Query("studentId") String studentId,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    /**
     * 根据学号获取该学生的详细信息
     */
    @GET("student/getStudentInfoByStudentId")
    Call<StudentInfoResult> getStudentInfoByStudentId(
            @Query("studentId") String studentId);


    /**
     * 根据学号修改学生密码
     */
    @FormUrlEncoded
    @POST("student/updatePassword")
    Call<CommonResult> updateStudentPassword(
            @Field("studentId") String studentId,
            @Field("newPassword") String newPassword);


    /**
     * 根据学号修改学生信息
     */
    @FormUrlEncoded
    @POST("student/updateStudentInfo")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Call<CommonResult> updateStudentInfo(
            @Field("studentId") String studentId,
            @Field("phone") String phone,
            @Field("address") String address);

    /**
     * 根据学号和子课程号获取学生的该课程的信息
     */
    @GET("studentCourse/getCourseInfo")
    Call<CourseInfoResult> getCourseInfo(
            @Query("studentId") String studentId,
            @Query("childCourseId") int childCourseId);

    /**
     * 获取教师的基本信息
     */
    @GET("teacher/getTeacherBaseByTeacherId")
    Call<TeacherInfoResult> getTeacherBaseByTeacherId(
            @Query("teacherId") int teacherId);


    //--------------   教师用户   --------------


    /**
     * 根据教师工号获取教师的详细信息
     */
    @GET("teacher/getTeacherInfoByTeacherId")
    Call<TeacherInfoResult> getTeacherInfoByTeacherId(
            @Query("teacherId") String teacherId);


    /**
     * 根据工号修改教师密码
     */
    @FormUrlEncoded
    @POST("teacher/updatePassword")
    Call<CommonResult> updateTeacherPassword(
            @Field("teacherId") String teacherId,
            @Field("newPassword") String newPassword);

    /**
     * 根据工号修改教师信息
     */
    @FormUrlEncoded
    @POST("teacher/updateTeacherInfo")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Call<CommonResult> updateTeacherInfo(
            @Field("teacherId") String teacherId,
            @Field("phone") String phone,
            @Field("address") String address);


    /**
     * 根据教师工号获取教师所教的所有课程
     */
    @GET("teacher/getChildCourseByTeacherId")
    Call<CourseListResult> getChildCourseByTeacherId(
            @Query("teacherId") String teacherId,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    /**
     * 根据子课程号获取该课程的所有学生
     */
    @GET("teacher/getStudentsInfoByChildCourseId")
    Call<StudentListResult> getStudentListByChildCourseId(
            @Query("childCourseId") int childCourseId);


    /**
     * 根据学号获取学生的基本信息
     */
    @GET("student/getStudentBaseInfoByStudentId")
    Call<StudentInfoResult> getStudentBaseInfoByStudentId(
            @Query("studentId") int studentId);


    /**
     * 根据学号和子课程号更新学生的成绩
     */
    @FormUrlEncoded
    @POST("studentCourse/updateScore")
    Call<CommonResult> updateStudentCourseScore(
            @Field("studentId") int studentId,
            @Field("childCourseId") int childCourseId,
            @Field("dailyScore") String dailyScore,
            @Field("examScore") String examScore,
            @Field("finalScore") int finalScore,
            @Field("credit") double credit,
            @Field("state") int state);


    //--------------  管理员用户   --------------

    //--------------  学生管理   ----------start

    /**
     * 获取指定页码的指定数量的学生
     */
    @GET("management/getStudentList")
    Call<ManageStudentListResult> getStudentList(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    /**
     * 获取指定页码的指定数量的教师
     */
    @GET("management/getTeacherList")
    Call<ManageTeacherListResult> getTeacherList(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);


    /**
     * 获取指定页码的指定数量的专业信息
     */
    @GET("management/getMajorList")
    Call<ManageMajorListResult> getMajorList(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    /**
     * 获取指定页码的指定数量的班级信息
     */
    @GET("management/getClassList")
    Call<ManageClassListResult> getClassList(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    /**
     * 添加班学生前，使用该接口获取正确的学生学号
     */
    @GET("student/getNewStudentId")
    Call<ManageIdResult> getNewStudentId(
            @Query("classId") int classId);

    /**
     * 添加学生
     */
    @Multipart
    @POST("management/insertStudent")
    Call<CommonResult> insertStudent(@Part MultipartBody.Part imagePart,
                                     @PartMap Map<String, RequestBody> partMap);

    /**
     * 更改学生
     */
    @Multipart
    @POST("management/updateStudent")
    Call<CommonResult> updateStudent(@Part MultipartBody.Part imagePart,
                                     @PartMap Map<String, RequestBody> partMap);

    //--------------  学生管理   ----------end


    //--------------  教师管理   ----------start

    /**
     * 添加教师前，使用该接口获取正确的教师Id
     */
    @GET("teacher/getNewTeacherId")
    Call<ManageIdResult> getNewTeacherId(
            @Query("departmentId") int departmentId);


    /**
     * 添加教师
     */
    @Multipart
    @POST("management/insertTeacher")
    Call<CommonResult> insertTeacher(@Part MultipartBody.Part imagePart,
                                     @PartMap Map<String, RequestBody> partMap);

    /**
     * 更改教师
     */
    @Multipart
    @POST("management/updateTeacher")
    Call<CommonResult> updateTeacher(@Part MultipartBody.Part imagePart,
                                     @PartMap Map<String, RequestBody> partMap);


    //--------------  教师管理   ----------end


    //--------------  院系管理   ----------start

    /**
     * 获取指定页码的指定数量的学院信息
     */
    @GET("management/getDepartmentList")
    Call<ManageDepartmentListResult> getDepartmentList(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    /**
     * 插入院系
     */
    @FormUrlEncoded
    @POST("management/insertDepartment")
    Call<CommonResult> insertDepartment(
            @Field("departmentId") String departmentId,
            @Field("name") String name,
            @Field("introductions") String introductions);


    /**
     * 添加院系前获取正确的院系编号
     */
    @GET("department/getNewDepartmentId")
    Call<ManageIdResult> getNewDepartmentId();

    /**
     * 根据院系Id获取院系的详细信息
     */
    @GET("management/getDepartmentInfoByDepartmentId")
    Call<DepartmentInfoResult> getDepartmentInfoByDepartmentId(
            @Query("departmentId") int departmentId);


    /**
     * 根据院系编号修改院系信息
     */
    @FormUrlEncoded
    @POST("management/updateDepartment")
    Call<CommonResult> updateDepartment(
            @Field("departmentId") int departmentId,
            @Field("name") String name,
            @Field("introductions") String introductions);

    //--------------  院系管理   ----------end


    //--------------  专业管理   ----------start


    /**
     * 添加专业前获取正确的专业编号
     */
    @GET("major/getNewMajorId")
    Call<ManageIdResult> getNewMajorId(
            @Query("departmentId") int departmentId);


    /**
     * 插入专业
     */
    @FormUrlEncoded
    @POST("management/insertMajor")
    Call<CommonResult> insertMajor(
            @Field("majorId") String majorId,
            @Field("departmentId") int departmentId,
            @Field("name") String name,
            @Field("introductions") String introductions);


    /**
     * 根据专业Id获取专业的详细信息
     */
    @GET("management/getMajorInfoByMajorId")
    Call<MajorInfoResult> getMajorInfoByMajorId(
            @Query("majorId") int majorId);


    /**
     * 更新专业信息
     */
    @FormUrlEncoded
    @POST("management/updateMajor")
    Call<CommonResult> updateMajor(
            @Field("majorId") int majorId,
            @Field("departmentId") int departmentId,
            @Field("name") String name,
            @Field("introductions") String introductions);


    //--------------  专业管理   ----------end

    //--------------  班级管理   ----------start

    /**
     * 更新班级信息
     */
    @FormUrlEncoded
    @POST("management/updateClass")
    Call<CommonResult> updateClass(
            @Field("classId") String classId,
            @Field("name") String name,
            @Field("grade") String grade,
            @Field("departmentId") int departmentId,
            @Field("majorId") int majorId);


    /**
     * 添加班级前，使用该接口获取正确的班级Id
     */
    @GET("class/getNewClassId")
    Call<ManageIdResult> getNewClassId(
            @Query("grade") String grade,
            @Query("majorId") int majorId);


    /**
     * 插入班级
     */
    @FormUrlEncoded
    @POST("management/insertClass")
    Call<CommonResult> insertClass(
            @Field("classId") int classId,
            @Field("name") String name,
            @Field("grade") String grade,
            @Field("departmentId") int departmentId,
            @Field("majorId") int majorId);

    /**
     * 根据班级Id获取班级的详细信息
     */
    @GET("management/getClassInfoByClassId")
    Call<ClassInfoResult> getClassInfoByClassId(
            @Query("classId") int classId);

    //--------------  班级管理   ----------end


    /**
     * 下载文件
     * 使用Streaming 方式 Retrofit 不会一次性将ResponseBody 读取进入内存，否则文件很多容易OOM
     * 使用自定义的一个InputStreamConverterFactory转换工厂将ResponseBody转成InputStream
     */
    @GET
    @Streaming
    Call<InputStream> downloadFile(@Url String url);

}
