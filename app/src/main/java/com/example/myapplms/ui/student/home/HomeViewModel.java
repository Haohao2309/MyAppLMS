package com.example.myapplms.ui.student.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplms.data.remote.dto.response.BannerResponse;
import com.example.myapplms.data.remote.dto.response.DashboardResponse;
import com.example.myapplms.data.repository.BannerRepository;
import com.example.myapplms.data.repository.CourseRepository;
import com.example.myapplms.data.repository.StudentRepository;
import com.example.myapplms.model.Course;
import com.example.myapplms.model.Student;
import com.example.myapplms.utils.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends ViewModel {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final BannerRepository bannerRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Resource<List<Course>>> _featuredCourses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> featuredCourses = _featuredCourses;

    private final MutableLiveData<Resource<List<Course>>> _recommendedCourses = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> recommendedCourses = _recommendedCourses;

    private final MutableLiveData<Resource<List<Course>>> _continueLearning = new MutableLiveData<>();
    public final LiveData<Resource<List<Course>>> continueLearning = _continueLearning;

    private final MutableLiveData<Resource<List<DashboardResponse.AchievementDTO>>> _achievements = new MutableLiveData<>();
    public final LiveData<Resource<List<DashboardResponse.AchievementDTO>>> achievements = _achievements;

    private final MutableLiveData<Resource<Student>> _studentProfile = new MutableLiveData<>();
    public final LiveData<Resource<Student>> studentProfile = _studentProfile;

    private final MutableLiveData<Resource<DashboardResponse>> _dashboardData = new MutableLiveData<>();
    public final LiveData<Resource<DashboardResponse>> dashboardData = _dashboardData;

    private final MutableLiveData<Resource<List<BannerResponse>>> _banners = new MutableLiveData<>();
    public final LiveData<Resource<List<BannerResponse>>> banners = _banners;

    public HomeViewModel(CourseRepository courseRepository,
                         StudentRepository studentRepository,
                         BannerRepository bannerRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.bannerRepository = bannerRepository;
    }

    public LiveData<Resource<DashboardResponse>> getDashboardData() {
        return dashboardData;
    }

    public void refreshData() {
        loadHomeData();
        loadBanners();
    }

    public void loadBanners() {
        bannerRepository.fetchActiveBanners(result -> _banners.postValue(result));
    }

    public void loadStudentProfile(Integer userId) {
        if (userId == null) return;
        _studentProfile.setValue(Resource.loading());
        executor.execute(() -> {
            Resource<Student> result = studentRepository.getStudentByUserId(userId);
            _studentProfile.postValue(result);
        });
    }

    public void loadHomeData() {
        _achievements.setValue(Resource.loading());
        courseRepository.getStudentDashboard().observeForever(result -> {
            _dashboardData.setValue(result);
            if (result.status == Resource.Status.SUCCESS && result.data != null) {
                DashboardResponse data = result.data;

                // Map Courses
                _featuredCourses.setValue(Resource.success(mapCourseResponses(data.featuredCourses)));
                _recommendedCourses.setValue(Resource.success(mapCourseResponses(data.recommendedCourses)));

                List<com.example.myapplms.data.remote.dto.response.CourseResponse> continueCourses =
                    data.continueLearningCourses != null ? data.continueLearningCourses : data.continueLearning;
                _continueLearning.setValue(Resource.success(mapCourseResponses(continueCourses)));

                // Set achievements
                _achievements.setValue(Resource.success(data.achievements));
            } else if (result.status == Resource.Status.ERROR) {
                _featuredCourses.setValue(Resource.error(result.message, null));
                _recommendedCourses.setValue(Resource.error(result.message, null));
                _continueLearning.setValue(Resource.error(result.message, null));
                _achievements.setValue(Resource.error(result.message, null));
            } else if (result.status == Resource.Status.LOADING) {
                _featuredCourses.setValue(Resource.loading());
                _recommendedCourses.setValue(Resource.loading());
                _continueLearning.setValue(Resource.loading());
                _achievements.setValue(Resource.loading());
            }
        });
    }

    private List<Course> mapCourseResponses(List<com.example.myapplms.data.remote.dto.response.CourseResponse> responses) {
        List<Course> list = new ArrayList<>();
        if (responses != null) {
            for (com.example.myapplms.data.remote.dto.response.CourseResponse res : responses) {
                list.add(Course.fromResponse(res));
            }
        }
        return list;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
