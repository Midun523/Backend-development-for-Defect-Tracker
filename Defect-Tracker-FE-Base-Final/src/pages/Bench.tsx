import React, { useState, useMemo, useEffect } from "react";
import { UserCheck, Calendar, Filter, UserPlus } from "lucide-react";
import { Card, CardContent, CardHeader } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Input } from "../components/ui/Input";
import { Modal } from "../components/ui/Modal";
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableCell,
} from "../components/ui/Table";
import { Badge } from "../components/ui/Badge";
import { DonutChart } from "../components/ui/DonutChart";
import { SearchableMultiSelect } from "../components/ui/SearchableMultiSelect";
import { Toast } from "../components/ui/Toast";
import { useNavigate } from "react-router-dom";
import { getBenchList, getEmployeeProjectHistory } from "../api/bench/bench";
import { getAllProjects } from "../api/projectget";
import { postProjectAllocations } from "../api/bench/projectAllocation";
import { usePermission } from "../context/PermissionContext";
import { OrbitProgress } from 'react-loading-indicators';
import { ROLE_TYPES } from "../enums/RoleType";
import { getAllRoles } from "../api/role/viewrole";

interface BenchEmployee {
  id: string;
  firstName: string;
  lastName: string;
  designation: string;
  availability: number;
  availabilityPeriod: string;
  email: string;
  phone: string;
  status: string;
  currentProjects: any[];
}

export const Bench: React.FC = () => {
  const navigate = useNavigate();
  
  const [employees, setEmployees] = useState<BenchEmployee[]>([]);
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [viewingEmployee, setViewingEmployee] = useState<BenchEmployee | null>(
    null,
  );

  // Allocation Modal State
  const [projects, setProjects] = useState<any[]>([]);
  const [isAllocateModalOpen, setIsAllocateModalOpen] = useState(false);
  const [selectedEmployeeForAllocation, setSelectedEmployeeForAllocation] = useState<BenchEmployee | null>(null);
  const [selectedProjectId, setSelectedProjectId] = useState<string>("");
  const [allocationRole, setAllocationRole] = useState<string>("Developer");
  const [availableRoles, setAvailableRoles] = useState<Array<{ value: string; label: string }>>(ROLE_TYPES);
  const [allocationPercent, setAllocationPercent] = useState<number>(100);
  const [allocationStartDate, setAllocationStartDate] = useState<string>(
    new Date().toISOString().split("T")[0]
  );
  const [allocationEndDate, setAllocationEndDate] = useState<string>("");
  const [isSubmittingAllocation, setIsSubmittingAllocation] = useState<boolean>(false);

  useEffect(() => {
    const loadRoles = async () => {
      try {
        const res = await getAllRoles(0, 100);
        const roles = res.data?.content || [];
        const customRoles = roles
          .filter((r: any) => r.roleName && !ROLE_TYPES.some((rt) => rt.label.toLowerCase() === r.roleName.toLowerCase()))
          .map((r: any) => ({
            value: r.type || r.roleName,
            label: r.roleName,
          }));
        setAvailableRoles([...ROLE_TYPES, ...customRoles]);
      } catch {
        setAvailableRoles(ROLE_TYPES);
      }
    };
    loadRoles();
  }, []);
  const [toast, setToast] = useState<{
    isOpen: boolean;
    message: string;
    type: "success" | "error";
  }>({
    isOpen: false,
    message: "",
    type: "success",
  });

  const [filters, setFilters] = useState({
    name: "",
    designation: [] as string[],
    availability: "",
    fromDate: "",
    toDate: "",
  });
  const [dateError, setDateError] = useState("");
  const [loading, setLoading] = useState(false);

  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  const {can} = usePermission();

  // All unique designations from employee list
  const allDesignations = useMemo(() => {
    const names = [
      ...new Set(employees.map((e) => e.designation).filter(Boolean)),
    ];
    return names.map((name, i) => ({ id: i + 1, name }));
  }, [employees]);

  const getAllBenchList = async () => {
    try {
      setLoading(true);
      const response = await getBenchList();

      const activeEmployees = response.filter((item: any) => {
        const isActive =
          item.active ??
          (item.employee?.active === true) ??
          (item.status?.toLowerCase() === "active");
        const avail = item.availability ?? item.employee?.availability ?? 0;
        return isActive && avail > 0;
      });

      const mappedEmployees = await Promise.all(
        activeEmployees.map(async (item: any) => {
          let currentProjects: any[] = [];
          const empObj = item.employee || item;
          const empId = String(empObj.id || item.id);

          try {
            const allocationResponse = await getEmployeeProjectHistory(empId);
            const allocations = allocationResponse?.data || [];
            const uniqueProjectMap = new Map<string, any>();

            allocations.forEach((alloc: any) => {
              if (alloc.status && alloc.status.toUpperCase() === "DEALLOCATED") return;
              const projectId =
                alloc.projectId ||
                alloc.project_id ||
                alloc.project?.id ||
                alloc.projectName ||
                alloc.project?.name;

              const projectName =
                alloc.projectName ||
                alloc.project?.name ||
                alloc.project_name ||
                "Unknown Project";

              if (!uniqueProjectMap.has(String(projectId))) {
                uniqueProjectMap.set(String(projectId), {
                  projectName,
                });
              }
            });

            currentProjects = Array.from(uniqueProjectMap.values());
          } catch (error) {
            currentProjects = [];
          }

          const desig =
            empObj.designationName ||
            item.designationName ||
            empObj.designation ||
            item.designation ||
            "Developer";
          const desigStr =
            typeof desig === "object"
              ? desig.designationName || "Developer"
              : String(desig);
          const avail = item.availability ?? empObj.availability ?? 0;

          return {
            id: empId,
            firstName: empObj.firstName || item.firstName || "",
            lastName: empObj.lastName || item.lastName || "",
            email: empObj.email || item.email || "",
            phone: empObj.contactNo || item.contactNo || empObj.phone || item.phone || "",
            designation: desigStr,
            availability: avail,
            availabilityPeriod: item.availabilityPeriod || "Immediate",
            status: "Active",
            currentProjects,
          };
        }),
      );

      setEmployees(mappedEmployees);
    } catch (error) {
      console.error("Error loading bench list:", error);
      setEmployees([]);
      setLoading(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    localStorage.removeItem("selectedProjectId");
    getAllBenchList();
    getAllProjects()
      .then((data: any) => {
        const list = Array.isArray(data)
          ? data
          : data?.data?.content || data?.data || [];
        setProjects(list);
        if (list.length > 0) {
          setSelectedProjectId(String(list[0].id));
        }
      })
      .catch((err) => {
        console.warn("Failed to load projects:", err);
      });
  }, []);

  const handleOpenAllocateModal = (emp: BenchEmployee) => {
    setSelectedEmployeeForAllocation(emp);
    setAllocationPercent(emp.availability || 100);
    setAllocationRole(emp.designation || "Developer");
    setAllocationStartDate(new Date().toISOString().split("T")[0]);
    setAllocationEndDate("");
    if (projects.length > 0 && !selectedProjectId) {
      setSelectedProjectId(String(projects[0].id));
    }
    setIsAllocateModalOpen(true);
  };

  const handleConfirmAllocation = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!selectedEmployeeForAllocation) return;
    if (!selectedProjectId) {
      setToast({
        isOpen: true,
        message: "Please select a project to allocate the employee to",
        type: "error",
      });
      return;
    }

    const percent = Number(allocationPercent);
    if (!percent || percent <= 0) {
      setToast({
        isOpen: true,
        message: "Allocation percentage must be greater than 0%",
        type: "error",
      });
      return;
    }
    if (percent > selectedEmployeeForAllocation.availability) {
      setToast({
        isOpen: true,
        message: `Allocation percentage cannot exceed employee availability (${selectedEmployeeForAllocation.availability}%)`,
        type: "error",
      });
      return;
    }

    try {
      setIsSubmittingAllocation(true);
      await postProjectAllocations({
        employeeId: Number(selectedEmployeeForAllocation.id),
        projectId: Number(selectedProjectId),
        role: allocationRole || "Developer",
        roleId: allocationRole === "Project Manager" ? 2 : 1,
        allocationPercentage: percent,
        allocationPercent: percent,
        startDate: allocationStartDate || new Date().toISOString().split("T")[0],
        endDate: allocationEndDate || undefined,
      });

      const assignedProj = projects.find(
        (p: any) => String(p.id) === String(selectedProjectId)
      );
      const projName =
        assignedProj?.projectName || assignedProj?.name || "the project";

      setToast({
        isOpen: true,
        message: `Successfully allocated ${selectedEmployeeForAllocation.firstName} to ${projName}!`,
        type: "success",
      });
      setIsAllocateModalOpen(false);
      setSelectedEmployeeForAllocation(null);
      await getAllBenchList();
    } catch (err: any) {
      const msg =
        err.response?.data?.message || err.message || "Failed to allocate employee";
      setToast({
        isOpen: true,
        message: `Allocation failed: ${msg}`,
        type: "error",
      });
    } finally {
      setIsSubmittingAllocation(false);
    }
  };

  function isDateInAvailablePeriod(
    availabilityPeriod: string,
    fromDate: string,
    toDate: string,
  ) {
    if (!fromDate && !toDate) return true;
    if (!availabilityPeriod) return false;

    const availableDate = new Date(availabilityPeriod);

    if (isNaN(availableDate.getTime())) return false;

    const selectedFrom = fromDate ? new Date(fromDate) : availableDate;
    const selectedTo = toDate ? new Date(toDate) : availableDate;

    return availableDate >= selectedFrom && availableDate <= selectedTo;
  }

  const filteredEmployees = useMemo(() => {
    let filtered = employees.filter((emp) => emp.availability > 0);
    if (filters.name.trim()) {
      const nameFilter = filters.name.trim().toLowerCase();
      filtered = filtered.filter((emp) => {
        const full = `${emp.firstName} ${emp.lastName}`.toLowerCase();
        return (
          full.startsWith(nameFilter) ||
          emp.firstName.toLowerCase().startsWith(nameFilter) ||
          emp.lastName.toLowerCase().startsWith(nameFilter)
        );
      });
    }
    if (filters.designation && filters.designation.length > 0) {
      filtered = filtered.filter((emp) =>
        filters.designation.includes(emp.designation),
      );
    }
    if (
      filters.availability &&
      filters.availability !== "All Availability" &&
      filters.availability !== ""
    ) {
      const minAvail = parseInt(filters.availability);
      if (!isNaN(minAvail))
        filtered = filtered.filter((emp) => emp.availability >= minAvail);
    }

    if (filters.fromDate || filters.toDate) {
      filtered = filtered.filter((emp) =>
        isDateInAvailablePeriod(
          emp.availabilityPeriod,
          filters.fromDate,
          filters.toDate,
        ),
      );
    }
    return filtered.sort((a, b) =>
      `${a.firstName} ${a.lastName}`.localeCompare(
        `${b.firstName} ${b.lastName}`,
      ),
    );
  }, [employees, filters]);

  const totalPages = Math.ceil(filteredEmployees.length / pageSize);
  const paginatedEmployees = filteredEmployees.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize,
  );

  const handleFilterChange = (field: string, value: string | string[]) => {
    if (field === "fromDate" || field === "toDate") setDateError("");
    if (field === "toDate" && value && filters.fromDate) {
      if (new Date(value as string) < new Date(filters.fromDate)) {
        setDateError("End date must be after the start date");
        setFilters((prev) => ({ ...prev, toDate: "" }));
        return;
      }
    }
    if (field === "fromDate" && value && filters.toDate) {
      if (new Date(filters.toDate) < new Date(value as string)) {
        setDateError("End date must be after the start date");
        setFilters((prev) => ({ ...prev, fromDate: "" }));
        return;
      }
    }
    setFilters((prev) => ({ ...prev, [field]: value }));
    setCurrentPage(1);
  };

  const handlePageSizeChange = (newSize: number) => {
    setPageSize(newSize);
    setCurrentPage(1); // Reset to first page when changing page size
  };

  const handleViewEmployee = (employee: BenchEmployee) => {
    setViewingEmployee(employee);
    setIsEmployeeModalOpen(true);
  };

  const getAvailabilityStatus = (availability: number) => {
    if (availability >= 80)
      return { label: "Highly Available", variant: "success" as const };
    if (availability >= 50)
      return { label: "Partially Available", variant: "warning" as const };
    return { label: "Busy", variant: "error" as const };
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Bench Management</h1>
          <p className="text-gray-600 mt-1">
            Manage employee availability and project allocations
          </p>
        </div>
      </div>

      {}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-wrap items-center gap-4">
            <div className="relative flex-1 min-w-[240px]">
              <Input
                placeholder="Search by name..."
                value={filters.name}
                onChange={(e) => handleFilterChange("name", e.target.value)}
                className="pl-10"
              />
            </div>
            <SearchableMultiSelect
              options={allDesignations.map((d) => ({
                value: d.name,
                label: d.name,
              }))}
              selectedValues={filters.designation}
              onChange={(values) => handleFilterChange("designation", values)}
              placeholder={
                allDesignations.length > 0
                  ? "All Designations"
                  : "No designations"
              }
              className="min-w-[200px]"
            />
            <select
              value={filters.availability || "All Availability"}
              onChange={(e) =>
                handleFilterChange("availability", e.target.value)
              }
              className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 min-w-[180px]"
            >
              <option value="">All Availability</option>
              <option value="80">80% and above</option>
              <option value="50">50% and above</option>
              <option value="30">30% and above</option>
              <option value="10">10% and above</option>
            </select>
            <div className="flex flex-col gap-1 min-w-[300px]">
              <div className="flex gap-2">
                <Input
                  type="date"
                  value={filters.fromDate}
                  onChange={(e) =>
                    handleFilterChange("fromDate", e.target.value)
                  }
                  placeholder="From Date"
                  className="flex-1"
                />
                <Input
                  type="date"
                  value={filters.toDate}
                  onChange={(e) => handleFilterChange("toDate", e.target.value)}
                  placeholder="To Date"
                  className="flex-1"
                />
              </div>
              {dateError && (
                <div className="text-red-600 text-sm">{dateError}</div>
              )}
            </div>
            <Button
              variant="secondary"
              onClick={() => {
                setFilters({
                  name: "",
                  designation: [],
                  availability: "",
                  fromDate: "",
                  toDate: "",
                });
                setDateError("");
              }}
              className="px-4 py-2"
            >
              Clear Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      {}
      {(filters.name ||
        filters.designation.length > 0 ||
        filters.availability ||
        filters.fromDate ||
        filters.toDate) && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <div className="flex items-center gap-2 text-sm text-blue-800">
            <Filter className="w-4 h-4" />
            <span className="font-medium">Active Filters:</span>
            {filters.name && (
              <Badge variant="info" size="sm">
                Name: {filters.name}
              </Badge>
            )}
            {filters.designation.length > 0 && (
              <Badge variant="info" size="sm">
                Designations: {filters.designation.join(", ")}
              </Badge>
            )}
            {filters.availability && (
              <Badge variant="info" size="sm">
                Availability: {filters.availability}% and above
              </Badge>
            )}
            {(filters.fromDate || filters.toDate) && (
              <Badge variant="info" size="sm">
                Date Range: {filters.fromDate || "Any"} to{" "}
                {filters.toDate || "Any"}
              </Badge>
            )}
          </div>
        </div>
      )}

      {}
      <div className="flex justify-end mb-2">
        {can.projectAllocation.view &&
        <Button
          variant="primary"
          className="ml-4"
          onClick={() => navigate("/bench-allocate")}
        >
          Allocate
        </Button>}
      </div>

      {}
      <Card className="shadow-lg">
        <CardHeader>
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-xl font-semibold text-gray-900">
                Employee Bench
              </h3>
              <p className="text-gray-600">
                Click on employee names to view detailed information
              </p>
            </div>
            {filteredEmployees.length > 0 && (
              <div className="text-sm text-gray-500">
                ({filteredEmployees.length} results)
              </div>
            )}
          </div>
        </CardHeader>
        {!loading ? <CardContent className="p-0">
          {filteredEmployees.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableCell header>Employee</TableCell>
                    <TableCell header>Designation</TableCell>
                    <TableCell header>Availability</TableCell>
                    <TableCell header>Available Period</TableCell>
                    <TableCell header>Current Projects</TableCell>
                    <TableCell header>Action</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {paginatedEmployees.map((employee) => {
                    const availabilityStatus = getAvailabilityStatus(
                        employee.availability,
                    );
                    return (
                        <TableRow key={employee.id}>
                          <TableCell>
                            <div className="flex items-center space-x-3">
                              <div className="w-10 h-10 bg-gradient-to-r from-blue-500 to-blue-600 rounded-full flex items-center justify-center">
                            <span className="text-white font-semibold text-sm">
                              {employee.firstName.charAt(0)}
                              {employee.lastName.charAt(0)}
                            </span>
                              </div>
                              <div>
                                <button
                                    onClick={() => handleViewEmployee(employee)}
                                    className="font-semibold text-blue-600 hover:text-blue-800 transition-colors duration-200"
                                >
                                  {employee.firstName} {employee.lastName}
                                </button>
                              </div>
                            </div>
                          </TableCell>
                          <TableCell>
                            <p className="font-medium text-gray-900">
                              {employee.designation}
                            </p>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center space-x-3">
                              <DonutChart
                                  percentage={employee.availability}
                                  size={50}
                                  strokeWidth={4}
                                  color={
                                    availabilityStatus.variant === "success"
                                        ? "#16a34a"
                                        : availabilityStatus.variant === "warning"
                                            ? "#eab308"
                                            : "#dc2626"
                                  }
                              />
                              <Badge variant={availabilityStatus.variant} size="sm">
                                {availabilityStatus.label}
                              </Badge>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="text-sm text-gray-600">
                              <div className="flex items-center space-x-2">
                                <Calendar className="w-4 h-4" />
                                <span>{employee.availabilityPeriod || "N/A"}</span>
                              </div>
                            </div>
                          </TableCell>
                          <TableCell>
                            <button
                                onClick={() => handleViewEmployee(employee)}
                                className="hover:scale-105 transition-transform"
                            >
                              {employee.currentProjects.length > 0 ? (
                                  <Badge variant="info" size="sm">
                                    {employee.currentProjects.length} Project
                                    {employee.currentProjects.length > 1 ? "s" : ""}
                                  </Badge>
                              ) : (
                                  <Badge variant="default" size="sm">
                                    No Projects
                                  </Badge>
                              )}
                            </button>
                          </TableCell>
                          <TableCell>
                            <Button
                              variant="primary"
                              size="sm"
                              onClick={() => handleOpenAllocateModal(employee)}
                              className="flex items-center gap-1.5 shadow-sm text-xs font-semibold px-3 py-1.5 whitespace-nowrap bg-blue-600 hover:bg-blue-700 text-white rounded-md"
                            >
                              <UserPlus className="w-3.5 h-3.5" />
                              <span>Allocate</span>
                            </Button>
                          </TableCell>
                        </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
          ) : (
              <div className="p-12 text-center">
                <UserCheck className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  {Object.entries(filters).some(
                      ([, v]) => v !== "" && !(Array.isArray(v) && v.length === 0),
                  )
                      ? "No employees match your filters"
                      : "No bench employees"}
                </h3>
                <p className="text-gray-500">
                  {Object.entries(filters).some(
                      ([, v]) => v !== "" && !(Array.isArray(v) && v.length === 0),
                  )
                      ? "Try adjusting your search filters"
                      : "Employees on bench will appear here"}
                </p>
              </div>
          )}
          {}
          {totalPages > 1 && (
              <div className="flex justify-between items-center gap-2 py-4 px-4">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-gray-600">Show:</span>
                  <select
                      value={pageSize}
                      onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                      className="border border-gray-300 rounded px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                    <option value={100}>100</option>
                  </select>
                  <span className="text-sm text-gray-600">per page</span>
                  <span className="text-sm text-gray-500 ml-2">
                  Showing {(currentPage - 1) * pageSize + 1} to{" "}
                    {Math.min(currentPage * pageSize, filteredEmployees.length)} of{" "}
                    {filteredEmployees.length}
                </span>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                      type="button"
                      variant="secondary"
                      onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                      disabled={currentPage === 1}
                  >
                    &lt;
                  </Button>
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                      (pageNum) => {
                        const isCurrent = pageNum === currentPage;
                        const isEdge = pageNum === 1 || pageNum === totalPages;
                        const isNear = Math.abs(pageNum - currentPage) <= 1;
                        if (isEdge || isNear)
                          return (
                              <button
                                  key={pageNum}
                                  className={`px-2 py-1 rounded text-sm font-medium ${
                                      isCurrent
                                          ? "bg-blue-600 text-white"
                                          : "bg-gray-200 text-gray-700 hover:bg-blue-100"
                                  }`}
                                  onClick={() => setCurrentPage(pageNum)}
                                  disabled={isCurrent}
                                  style={{ minWidth: 32 }}
                              >
                                {pageNum}
                              </button>
                          );
                        if (pageNum === 2 && currentPage > 3)
                          return (
                              <span key="s-ellipsis" className="px-2">
                          ...
                        </span>
                          );
                        if (
                            pageNum === totalPages - 1 &&
                            currentPage < totalPages - 2
                        )
                          return (
                              <span key="e-ellipsis" className="px-2">
                          ...
                        </span>
                          );
                        return null;
                      },
                  )}
                  <Button
                      type="button"
                      variant="secondary"
                      onClick={() =>
                          setCurrentPage((p) => Math.min(totalPages, p + 1))
                      }
                      disabled={currentPage === totalPages}
                  >
                    &gt;
                  </Button>
                </div>
              </div>
          )}

          {}
          {totalPages === 1 && filteredEmployees.length > 0 && (
              <div className="flex justify-end items-center py-2 px-4">
              <span className="text-sm text-gray-500">
                Showing {filteredEmployees.length} of {filteredEmployees.length} results
              </span>
              </div>
          )}
        </CardContent> :
            <div className="flex justify-center items-center py-20">
              <OrbitProgress
                  variant="dotted"
                  color="#3B82F6"
                  size="medium"
                  text=""
                  textColor=""
              />
            </div>
        }

      </Card>

      {/* Employee Details Modal */}
      <Modal
        isOpen={isEmployeeModalOpen}
        onClose={() => {
          setIsEmployeeModalOpen(false);
          setViewingEmployee(null);
        }}
        title="Employee Details"
        size="xl"
      >
        {viewingEmployee && (
          <div className="space-y-6">
            <div className="flex items-center space-x-6">
              <div className="w-20 h-20 bg-gradient-to-r from-blue-500 to-blue-600 rounded-full flex items-center justify-center">
                <span className="text-white font-bold text-2xl">
                  {viewingEmployee.firstName.charAt(0)}
                  {viewingEmployee.lastName.charAt(0)}
                </span>
              </div>
              <div className="flex-1">
                <h3 className="text-2xl font-bold text-gray-900">
                  {viewingEmployee.firstName} {viewingEmployee.lastName}
                </h3>
                <p className="text-lg text-gray-600">
                  {viewingEmployee.designation}
                </p>
                <div className="flex items-center space-x-4 mt-2">
                  <Badge
                    variant={
                      getAvailabilityStatus(viewingEmployee.availability)
                        .variant
                    }
                  >
                    {getAvailabilityStatus(viewingEmployee.availability).label}
                  </Badge>
                </div>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <p>
                <span className="font-medium">Email:</span>{" "}
                {viewingEmployee.email || "-"}
              </p>
              <p>
                <span className="font-medium">Phone:</span>{" "}
                {viewingEmployee.phone || "-"}
              </p>
              <p>
                <span className="font-medium">Availability:</span>{" "}
                {viewingEmployee.availability}%
              </p>
              <p>
                <span className="font-medium">Current Projects:</span>{" "}
                {viewingEmployee.currentProjects.length > 0
                  ? viewingEmployee.currentProjects
                      .map((project: any) => project.projectName)
                      .join(", ")
                  : "None"}
              </p>
            </div>
          </div>
        )}
      </Modal>

      {/* Allocate to Project Modal */}
      <Modal
        isOpen={isAllocateModalOpen}
        onClose={() => {
          setIsAllocateModalOpen(false);
          setSelectedEmployeeForAllocation(null);
        }}
        title="Allocate Employee to Project"
        size="lg"
      >
        {selectedEmployeeForAllocation && (
          <form onSubmit={handleConfirmAllocation} className="space-y-5">
            {/* Employee Preview */}
            <div className="flex items-center justify-between p-4 bg-blue-50 border border-blue-100 rounded-xl">
              <div className="flex items-center space-x-3">
                <div className="w-12 h-12 bg-gradient-to-tr from-blue-600 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold text-lg shadow-sm">
                  {selectedEmployeeForAllocation.firstName.charAt(0)}
                  {selectedEmployeeForAllocation.lastName.charAt(0)}
                </div>
                <div>
                  <h4 className="font-semibold text-gray-900 text-base">
                    {selectedEmployeeForAllocation.firstName} {selectedEmployeeForAllocation.lastName}
                  </h4>
                  <p className="text-sm text-gray-600">
                    {selectedEmployeeForAllocation.designation} &bull; {selectedEmployeeForAllocation.email}
                  </p>
                </div>
              </div>
              <div className="text-right">
                <span className="text-xs text-gray-500 uppercase tracking-wider font-semibold">Available</span>
                <div className="text-xl font-bold text-blue-600">
                  {selectedEmployeeForAllocation.availability}%
                </div>
              </div>
            </div>

            {/* Project Selection */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1.5">
                Select Project <span className="text-red-500">*</span>
              </label>
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                required
                className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white text-gray-900 text-sm"
              >
                <option value="" disabled>-- Select a project --</option>
                {projects.map((proj: any) => (
                  <option key={proj.id} value={proj.id}>
                    {proj.projectName || proj.name} {proj.clientName ? `(${proj.clientName})` : ""}
                  </option>
                ))}
              </select>
            </div>

            {/* Role Selection */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1.5">
                  Role in Project <span className="text-red-500">*</span>
                </label>
                <select
                  value={allocationRole}
                  onChange={(e) => setAllocationRole(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none bg-white text-gray-900 text-sm"
                >
                  {availableRoles.map((rt) => (
                    <option key={rt.value || rt.label} value={rt.label}>
                      {rt.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* Allocation Percentage */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="block text-sm font-semibold text-gray-700">
                    Allocation Percentage <span className="text-red-500">*</span>
                  </label>
                  <span className="text-xs text-gray-500">
                    Max: {selectedEmployeeForAllocation.availability}%
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <Input
                    type="number"
                    min="1"
                    max={selectedEmployeeForAllocation.availability}
                    value={allocationPercent}
                    onChange={(e) =>
                      setAllocationPercent(
                        Math.min(
                          selectedEmployeeForAllocation.availability,
                          Math.max(1, Number(e.target.value))
                        )
                      )
                    }
                    className="w-24 text-center font-bold"
                  />
                  <span className="text-sm font-semibold text-gray-600">%</span>
                  <div className="flex gap-1 ml-auto">
                    {[25, 50, 75, 100].map((pct) => {
                      if (
                        pct > selectedEmployeeForAllocation.availability &&
                        pct !== 100
                      )
                        return null;
                      const targetPct = Math.min(
                        pct,
                        selectedEmployeeForAllocation.availability
                      );
                      return (
                        <button
                          key={pct}
                          type="button"
                          onClick={() => setAllocationPercent(targetPct)}
                          className={`text-xs px-2 py-1 rounded border transition-colors ${
                            allocationPercent === targetPct
                              ? "bg-blue-600 text-white border-blue-600"
                              : "bg-gray-50 hover:bg-gray-100 text-gray-700 border-gray-200"
                          }`}
                        >
                          {pct === 100
                            ? `${selectedEmployeeForAllocation.availability}%`
                            : `${pct}%`}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
            </div>

            {/* Dates */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1.5">
                  Start Date
                </label>
                <Input
                  type="date"
                  value={allocationStartDate}
                  onChange={(e) => setAllocationStartDate(e.target.value)}
                  className="w-full"
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1.5">
                  End Date
                </label>
                <Input
                  type="date"
                  value={allocationEndDate}
                  onChange={(e) => setAllocationEndDate(e.target.value)}
                  className="w-full"
                />
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setIsAllocateModalOpen(false);
                  setSelectedEmployeeForAllocation(null);
                }}
                disabled={isSubmittingAllocation}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="primary"
                disabled={isSubmittingAllocation || !selectedProjectId}
                className="flex items-center gap-2"
              >
                {isSubmittingAllocation ? (
                  <span>Allocating...</span>
                ) : (
                  <>
                    <UserPlus className="w-4 h-4" />
                    <span>Add to Project</span>
                  </>
                )}
              </Button>
            </div>
          </form>
        )}
      </Modal>

      {/* Toast Notification */}
      <Toast
        isOpen={toast.isOpen}
        message={toast.message}
        type={toast.type}
        onClose={() => setToast((prev) => ({ ...prev, isOpen: false }))}
      />
    </div>
  );
};