package com.dobie.backend.domain.project.service;

import com.dobie.backend.domain.docker.dockercompose.service.DockerComposeService;
import com.dobie.backend.domain.docker.dockerfile.service.DockerfileService;
import com.dobie.backend.domain.nginx.service.NginxConfigService;
import com.dobie.backend.domain.project.dto.*;
import com.dobie.backend.domain.project.entity.Backend;
import com.dobie.backend.domain.project.entity.Database;
import com.dobie.backend.domain.project.entity.Frontend;
import com.dobie.backend.domain.project.entity.Project;
import com.dobie.backend.domain.project.repository.ProjectRepository;
import com.dobie.backend.exception.exception.build.*;
import com.dobie.backend.exception.exception.file.SaveFileFailedException;
import com.dobie.backend.exception.exception.git.GitInfoNotFoundException;
import com.dobie.backend.util.command.CommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CommandService commandService;
    private final DockerfileService dockerfileService;
    private final DockerComposeService dockerComposeService;
    private final NginxConfigService nginxConfigService;

    @Override
    public void createProject(ProjectRequestDto dto) {
        Project project = new Project(UUID.randomUUID().toString(), dto);
        projectRepository.upsertProject(project);
    }

    @Override
    public Map<String, ProjectGetResponseDto> getAllProjects() {
        Map<String, Project> map = projectRepository.selectProjects();
        Map<String, ProjectGetResponseDto> resultMap = new HashMap<>();
        map.forEach((key, value) -> {
            resultMap.put(key, new ProjectGetResponseDto(value));
        });
        return resultMap;
    }

    @Override
    public ProjectGetResponseDto getProject(String projectId) {
        Project project = projectRepository.searchProject(projectId);
        return new ProjectGetResponseDto(project);
    }

    @Override
    public List<BackendGetResponseDto> getAllBackends(String projectId) {
        Map<String, Backend> backendMap = projectRepository.selectBackends(projectId);
        List<BackendGetResponseDto> list = new ArrayList<>();
        backendMap.forEach((key, value) -> {
            list.add(new BackendGetResponseDto(value));
        });
        return list;
    }

    @Override
    public BackendGetResponseDto getBackend(String projectId, String serviceId) {
        Backend backend = projectRepository.searchBackend(projectId, serviceId);
        return new BackendGetResponseDto(backend);
    }

    @Override
    public FrontendGetResponseDto getFrontend(String projectId) {
        Frontend frontend = projectRepository.searchFrontend(projectId);
        return new FrontendGetResponseDto(frontend);
    }

    @Override
    public DatabaseGetResponseDto getDatabase(String projectId, String databaseId) {
        Database database = projectRepository.searchDatabase(projectId, databaseId);
        return new DatabaseGetResponseDto(database);
    }

    @Override
    public Map<String, DatabaseGetResponseDto> getAllDatabases(String projectId) {
        Map<String, Database> databaseMap = projectRepository.selectDatabases(projectId);
        Map<String, DatabaseGetResponseDto> dtoMap = new HashMap<>();
        databaseMap.forEach((key, value) -> {
            dtoMap.put(key, new DatabaseGetResponseDto(value));
        });
        return dtoMap;
    }

    @Override
    public void updateProject(String projectId, ProjectRequestDto dto) {
        Project project = new Project(projectId, dto);
        projectRepository.upsertProject(project);
    }

    @Override
    public void deleteProject(String projectId) {
        projectRepository.deleteProject(projectId);
    }


    /*
     * 프로젝트 실행 관련 메서드들
     * */


    // 전체 프로젝트(main 브랜치에서 한번에 관리) 빌드 메서드
    // 사실상 dockerfile이랑 compose file 넣어놓는 용도
    @Override
    public void buildTotalService(String projectId) {
        ProjectGetResponseDto projectGetResponseDto = getProject(projectId);

        // git clone
        GitGetResponseDto gitInfo = projectGetResponseDto.getGit();

        if (gitInfo == null) {
            throw new GitInfoNotFoundException();
        }

        // git type 확인, gitLab인지 gitHub인지
        // 1이면 gitLab
        if (gitInfo.getGitType() == 1) {
            if (!commandService.checkIsCloned("./" + projectGetResponseDto.getProjectName())) {
                // gitLab clone
                commandService.gitCloneGitLab(gitInfo.getGitUrl(), gitInfo.getAccessToken());
            }
        } else {
            if (!commandService.checkIsCloned("./" + projectGetResponseDto.getProjectName())) {
                // gitHub Clone
                commandService.gitClone(gitInfo.getGitUrl());
            }
        }

        // dockerfile 생성
        // 백엔드
        Map<String, BackendGetResponseDto> backendInfo = projectGetResponseDto.getBackendMap();
        backendInfo.forEach((key, value) -> {
            if (value.getFramework().equals("SpringBoot(Gradle)")) {
                dockerfileService.createGradleDockerfile(projectGetResponseDto.getProjectName(), value.getVersion(), value.getPath());
            } else if (value.getFramework().equals("SpringBoot(Maven)")) {
                dockerfileService.createMavenDockerfile(projectGetResponseDto.getProjectName(), value.getVersion(), value.getPath());
            }
        });


        // 프론트엔드
        FrontendGetResponseDto frontendInfo = projectGetResponseDto.getFrontend();
        if (frontendInfo.getFramework().equals("React")) {
            dockerfileService.createReactDockerfile(projectGetResponseDto.getProjectName(), frontendInfo.getVersion(), frontendInfo.getPath());
        } else if (frontendInfo.getFramework().equals("Vue")) {
            dockerfileService.createVueDockerfile(projectGetResponseDto.getProjectName(), frontendInfo.getVersion(), frontendInfo.getPath());
        }

        // docker-compose 파일 생성
        dockerComposeService.createDockerComposeFile(projectGetResponseDto);

        //nginx proxy config 파일생성
        nginxConfigService.saveProxyNginxConfig(projectId);

        try {
            //frontend nginx config 파일 저장
            nginxConfigService.saveFrontNginxConfigFile(projectGetResponseDto.getFrontend().getPath(), projectGetResponseDto.getProjectName());
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SaveFileFailedException("front nginx config 파일 저장에 실패했습니다."); //예외처리
        }


    }

    // 프로젝트 통째로 실행한다 했을때
    @Override
    public CompletableFuture<Void> runProject(String projectId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ProjectGetResponseDto projectGetResponseDto = getProject(projectId);
                // git clone 받으면 projectName으로 폴더가 생성되어 있을테니
                String path = "./" + projectGetResponseDto.getProjectName();
                commandService.dockerComposeUp(path);

                Thread.sleep(10000);

                if(!verifyComposeUpSuccess(path)){
                    throw new ProjectStartFailedException("compose up failed");
                }
            }catch (Exception e){
                throw new ProjectStartFailedException(e.getMessage());
            }
        });
    }

    @Override
    public void stopProject(String projectId) {
        ProjectGetResponseDto projectGetResponseDto = getProject(projectId);
        String path = "./" + projectGetResponseDto.getProjectName();
        commandService.dockerComposeDown(path);
    }

    private boolean verifyComposeUpSuccess(String path) {
        try {
            String command = "docker compose -f " + path + "/docker-compose.yml ps";
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Up")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new ProjectStartFailedException(e.getMessage());
        }
    }

//    @Override
//    public void stopService(String projectId) {
//        ProjectGetResponseDto projectGetResponseDto = getProject(projectId);
//        String containerName = "temp";
//        commandService.dockerStop(containerName);
//    }

//    // 프론트 개별 빌드
//    void buildFrontService(String projectId, ProjectRequestDto dto) {
//
//        Project project = projectRepository.searchProject(projectId);
//
//        // git clone
//        GitRequestDto gitInfo = dto.getGit();
//        // git type 확인, gitLab인지 gitHub인지
//        // 1이면 gitLab
//        if (gitInfo.getGitType() == 1) {
//            // gitLab clone
//            commandService.gitCloneGitLab(gitInfo.getGitUrl(), gitInfo.getAccessToken());
//        } else {
//            // gitHub Clone
//            commandService.gitClone(gitInfo.getGitUrl());
//        }
//
//        FrontendRequestDto frontendInfo = dto.getFrontend();
//
//        // checkout
//        commandService.gitCheckout(frontendInfo.getPath(), frontendInfo.getBranch());
//
//        // dockerfile
//        if (frontendInfo.getFramework() == "React") {
//            dockerfileService.createReactDockerfile(frontendInfo.getServiceName(), frontendInfo.getVersion(), frontendInfo.getPath());
//        }
//
//        // docker-compose
//        ProjectGetResponseDto projectGetResponseDto = new ProjectGetResponseDto(project);
//        dockerComposeService.createDockerComposeFile(projectGetResponseDto);
//    }

//    // 백엔드 개별 빌드
//    void buildBackService(String projectId, ProjectRequestDto dto) {
//
//        Project project = projectRepository.searchProject(projectId);
//
//        GitRequestDto gitInfo = dto.getGit();
//        if (gitInfo.getGitType() == 1) {
//            commandService.gitCloneGitLab(gitInfo.getGitUrl(), gitInfo.getAccessToken());
//        } else {
//            commandService.gitClone(gitInfo.getGitUrl());
//        }
//
//        Map<String, BackendRequestDto> backendInfo = dto.getBackendMap();
//        backendInfo.forEach((key, value) -> {
//
//            // git checkout
//            commandService.gitCheckout(value.getPath(), value.getBranch());
//
//            // dockerfile
//            // home/projectName+path
//            if (value.getFramework() == "Spring") {
//                dockerfileService.createGradleDockerfile(project.getProjectName(), value.getVersion(), value.getPath());
//            }
//        });
//
//        // docker compose 파일 생성 -> 이거 좀 햇갈리는게 각각 실행시키고 백엔드 서비스가 여러개 있을때 docker-compose는 여러개인가 하나인가
//        ProjectGetResponseDto projectGetResponseDto = new ProjectGetResponseDto(project);
//        dockerComposeService.createDockerComposeFile(projectGetResponseDto);
//    }

}

