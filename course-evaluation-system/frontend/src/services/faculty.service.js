import http from './http.service'

class FacultyService {
  getFaculties() {
    return http.get('faculties')
  }
  
  getFaculty(id) {
    return http.get(`faculties/${id}`)
  }
  
  createFaculty(data) {
    return http.post('faculties', data)
  }
  
  updateFaculty(id, data) {
    return http.put(`faculties/${id}`, data)
  }
  
  deleteFaculty(id) {
    return http.delete(`faculties/${id}`)
  }
}

export default new FacultyService() 