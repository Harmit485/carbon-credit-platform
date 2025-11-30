import { useEffect, useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../config/api';
import GlassCard from '../ui/GlassCard';
import Button from '../ui/Button';
import InputField from '../ui/InputField';
import SelectField from '../ui/SelectField';
import Modal from '../ui/Modal';
import Tag from '../ui/Tag';

const FILTERS = [
  { id: 'ALL', label: 'All' },
  { id: 'VERIFIED', label: 'Verified' },
  { id: 'PENDING', label: 'Pending review' },
  { id: 'MY_PROJECTS', label: 'My projects' }
];

const PROJECT_TYPES = [
  'RENEWABLE_ENERGY',
  'SOLAR',
  'WIND',
  'REFORESTATION',
  'FORESTRY',
  'AGRICULTURE',
  'WASTE_MANAGEMENT',
  'ENERGY_EFFICIENCY',
  'MANUFACTURING',
  'METAL',
  'BURNING',
  'OTHER'
];

const STATUS_VARIANT = {
  VERIFIED: 'success',
  PENDING: 'warning',
  REJECTED: 'danger'
};

const Projects = ({ user }) => {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [selectedProject, setSelectedProject] = useState(null);
  const [showIssueForm, setShowIssueForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    location: '',
    year: new Date().getFullYear(),
    type: 'RENEWABLE_ENERGY',
    totalCarbonCredits: 0,
    documentationUrl: ''
  });
  const [issueFormData, setIssueFormData] = useState({
    projectId: '',
    quantity: 0,
    vintageYear: new Date().getFullYear(),
    pricePerUnit: 0
  });

  const refreshProjects = async () => {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_BASE_URL}/projects`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    setProjects(response.data);
  };

  useEffect(() => {
    const init = async () => {
      try {
        await refreshProjects();
      } catch (error) {
        console.error('Error fetching projects:', error);
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      await axios.post(`${API_BASE_URL}/projects`, formData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setFormData({
        name: '',
        description: '',
        location: '',
        year: new Date().getFullYear(),
        type: 'RENEWABLE_ENERGY',
        totalCarbonCredits: 0,
        documentationUrl: ''
      });
      setShowForm(false);
      await refreshProjects();
      alert('Project created successfully! It is now PENDING verification.');
    } catch (error) {
      console.error('Error creating project:', error);
      alert('Failed to create project. Please try again.');
    }
  };

  const handleIssueSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      await axios.post(
        `${API_BASE_URL}/credits`,
        {
          projectId: issueFormData.projectId,
          quantity: parseFloat(issueFormData.quantity),
          vintageYear: parseInt(issueFormData.vintageYear, 10),
          pricePerUnit: parseFloat(issueFormData.pricePerUnit),
          ownerId: user.id,
          status: 'ISSUED'
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIssueFormData({
        projectId: '',
        quantity: 0,
        vintageYear: new Date().getFullYear(),
        pricePerUnit: 0
      });
      setShowIssueForm(false);
      alert('Credits issued successfully! They are now pending verification.');
    } catch (error) {
      console.error('Error issuing credits:', error);
      alert('Failed to issue credits. Please try again.');
    }
  };

  const handleVerifyProject = async (projectId, status) => {
    try {
      const token = localStorage.getItem('token');
      await axios.put(
        `${API_BASE_URL}/projects/${projectId}/verify`,
        { status },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      await refreshProjects();
      setSelectedProject(null);
      alert(`Project ${status.toLowerCase()} successfully!`);
    } catch (error) {
      console.error('Error verifying project:', error);
      alert(`Failed to ${status.toLowerCase()} project.`);
    }
  };

  const filteredProjects = projects.filter((project) => {
    if (filterStatus === 'ALL') return true;
    if (filterStatus === 'MY_PROJECTS') return project.issuerId === user?.id;
    return project.status === filterStatus;
  });

  const isVerifier = Array.isArray(user?.roles) && user.roles.includes('ROLE_ADMIN');

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center text-white/70">
        Loading projects...
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <GlassCard
        variant="canopy"
        title="Carbon Impact Projects"
        subtitle="Explore environmental projects that generate (produce) or offset (reduce) carbon emissions."
        action={
          <div className="flex flex-wrap gap-2">
            {FILTERS.map((filter) => (
              <Button
                key={filter.id}
                size="sm"
                variant={filterStatus === filter.id ? 'primary' : 'secondary'}
                onClick={() => setFilterStatus(filter.id)}
              >
                {filter.label}
              </Button>
            ))}
            <Button size="sm" onClick={() => setShowForm((prev) => !prev)}>
              {showForm ? 'Close form' : 'Add project'}
            </Button>
          </div>
        }
      />

      {showForm && (
        <GlassCard variant="subtle" title="Create new project">
          <form onSubmit={handleSubmit} className="grid gap-5 md:grid-cols-2">
            <InputField
              label="Project name"
              name="name"
              value={formData.name}
              onChange={(e) => setFormData((prev) => ({ ...prev, name: e.target.value }))}
              placeholder="Community solar expansion – Gujarat"
              required
            />
            <InputField
              label="Location"
              name="location"
              value={formData.location}
              onChange={(e) => setFormData((prev) => ({ ...prev, location: e.target.value }))}
              placeholder="Ahmedabad, Gujarat, India"
              required
            />
            <InputField
              label="Year"
              name="year"
              type="number"
              min="2000"
              max={new Date().getFullYear() + 5}
              value={formData.year}
              onChange={(e) => setFormData((prev) => ({ ...prev, year: e.target.value }))}
              required
            />
            <SelectField
              label="Project type"
              name="type"
              value={formData.type}
              onChange={(e) => setFormData((prev) => ({ ...prev, type: e.target.value }))}
            >
              {PROJECT_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type.replaceAll('_', ' ')}
                </option>
              ))}
            </SelectField>
            <InputField
              label="Estimated total credits"
              name="totalCarbonCredits"
              type="number"
              min="1"
              value={formData.totalCarbonCredits}
              onChange={(e) => setFormData((prev) => ({ ...prev, totalCarbonCredits: e.target.value }))}
              required
            />
            <InputField
              label="Documentation URL"
              name="documentationUrl"
              type="url"
              value={formData.documentationUrl}
              onChange={(e) => setFormData((prev) => ({ ...prev, documentationUrl: e.target.value }))}
              placeholder="https://..."
            />
            <div className="md:col-span-2">
              <label className="mb-2 block text-sm font-medium text-white/90">Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={(e) => setFormData((prev) => ({ ...prev, description: e.target.value }))}
                rows="4"
                className="glass-input"
                placeholder="Explain the carbon methodology, expected outcomes, and community benefits."
                required
              />
            </div>
            <div className="md:col-span-2 flex justify-end">
              <Button type="submit">Submit for review</Button>
            </div>
          </form>
        </GlassCard>
      )}

      <div className="space-y-5">
        {filteredProjects.map((project) => (
          <ProjectCard
            key={project.id}
            project={project}
            onView={() => setSelectedProject(project)}
            onIssue={() => {
              setIssueFormData((prev) => ({ ...prev, projectId: project.id }));
              setShowIssueForm(true);
            }}
            canIssue={user?.id === project.issuerId && project.status === 'VERIFIED'}
          />
        ))}

        {filteredProjects.length === 0 && (
          <GlassCard variant="subtle">
            <p className="text-white/70">No projects match the current filter.</p>
            <Button className="mt-4" variant="secondary" onClick={() => setFilterStatus('ALL')}>
              Reset filters
            </Button>
          </GlassCard>
        )}
      </div>

      <Modal
        open={Boolean(selectedProject)}
        onClose={() => setSelectedProject(null)}
        title={selectedProject?.name}
        subtitle={selectedProject ? `${selectedProject.location} · ${selectedProject.year}` : ''}
        actions={[
          ...(isVerifier && selectedProject?.status === 'PENDING'
            ? [
              <Button
                key="reject"
                variant="secondary"
                onClick={() => handleVerifyProject(selectedProject.id, 'REJECTED')}
              >
                Reject
              </Button>,
              <Button
                key="approve"
                onClick={() => handleVerifyProject(selectedProject.id, 'VERIFIED')}
              >
                Approve
              </Button>
            ]
            : []),
          <Button key="close" variant="secondary" onClick={() => setSelectedProject(null)}>
            Close
          </Button>
        ]}
      >
        {selectedProject && (
          <div className="space-y-6">
            <div className="flex flex-wrap items-center gap-3">
              <Tag variant={STATUS_VARIANT[selectedProject.status] || 'neutral'}>
                {statusLabel(selectedProject.status)}
              </Tag>
              <Tag variant="accent">{selectedProject.type?.replaceAll('_', ' ')}</Tag>
            </div>
            <p className="text-white/70 whitespace-pre-line">{selectedProject.description}</p>
            <div className="grid gap-4 sm:grid-cols-2">
              <Stat label="Total credits" value={selectedProject.totalCarbonCredits?.toLocaleString() || '—'} />
              <Stat
                label="Available credits"
                value={
                  selectedProject.availableCarbonCredits !== undefined
                    ? selectedProject.availableCarbonCredits.toLocaleString()
                    : '—'
                }
              />
              <Stat label="Documentation" value={selectedProject.documentationUrl ? 'Ready' : 'Not provided'} />
              {selectedProject.owner?.name && (
                <Stat
                  label="Project owner"
                  value={`${selectedProject.owner.name}${selectedProject.owner.organization ? ` · ${selectedProject.owner.organization}` : ''
                    }`}
                />
              )}
            </div>
            {selectedProject.documentationUrl && (
              <Button
                as="a"
                href={selectedProject.documentationUrl}
                target="_blank"
                rel="noopener noreferrer"
                variant="secondary"
              >
                View documentation
              </Button>
            )}
          </div>
        )}
      </Modal>

      <Modal
        open={showIssueForm}
        onClose={() => setShowIssueForm(false)}
        title="Issue carbon credits"
        subtitle="Issue carbon credits for verified projects. Each credit represents 1 tonne CO₂ equivalent. Credits are recorded in the ledger and can be traded on the marketplace."
        actions={[
          <Button key="cancel" variant="secondary" onClick={() => setShowIssueForm(false)}>
            Cancel
          </Button>,
          <Button key="issue" type="submit" form="issue-form">
            Issue credits
          </Button>
        ]}
      >
        <form id="issue-form" onSubmit={handleIssueSubmit} className="space-y-4">
          <InputField
            label="Quantity"
            name="quantity"
            type="number"
            min="1"
            value={issueFormData.quantity}
            onChange={(e) => setIssueFormData((prev) => ({ ...prev, quantity: e.target.value }))}
            required
          />
          <InputField
            label="Vintage year"
            name="vintageYear"
            type="number"
            min="2000"
            value={issueFormData.vintageYear}
            onChange={(e) => setIssueFormData((prev) => ({ ...prev, vintageYear: e.target.value }))}
            required
          />
          <InputField
            label="Price per credit (INR)"
            name="pricePerUnit"
            type="number"
            step="0.01"
            min="0.01"
            value={issueFormData.pricePerUnit}
            onChange={(e) => setIssueFormData((prev) => ({ ...prev, pricePerUnit: e.target.value }))}
            required
          />
        </form>
      </Modal>
    </div>
  );
};

const statusLabel = (status) => {
  if (!status) return 'Unknown';
  return status.toUpperCase() === 'PENDING' ? 'Awaiting verification' : status;
};

const ProjectCard = ({ project, onView, onIssue, canIssue }) => (
  <div className="glass-panel relative overflow-hidden rounded-3xl border border-emerald-400/20 bg-gradient-to-r from-emerald-500/10 via-transparent to-transparent p-5 sm:p-6">
    <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div className="space-y-3">
        <div className="flex flex-wrap items-center gap-3">
          <h3 className="text-2xl font-semibold">{project.name}</h3>
          <Tag variant={STATUS_VARIANT[project.status] || 'neutral'}>
            {statusLabel(project.status)}
          </Tag>
          <Tag variant="accent">{project.year}</Tag>
        </div>
        <p className="text-white/70">{project.location}</p>
        <div className="flex flex-wrap gap-4 text-sm text-white/60">
          <span>Total credits: {project.totalCarbonCredits?.toLocaleString() || '—'}</span>
          {project.availableCarbonCredits !== undefined && (
            <span>Available: {project.availableCarbonCredits.toLocaleString()}</span>
          )}
        </div>
        <p className="text-white/60">
          {project.description
            ? `${project.description.slice(0, 180)}${project.description.length > 180 ? '…' : ''}`
            : 'No description provided yet.'}
        </p>
      </div>
      <div className="flex flex-col gap-2 sm:flex-row">
        <Button variant="secondary" size="sm" onClick={onView}>
          View details
        </Button>
        {canIssue && (
          <Button size="sm" onClick={onIssue}>
            Issue credits
          </Button>
        )}
      </div>
    </div>
  </div>
);

const Stat = ({ label, value }) => (
  <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
    <p className="text-xs uppercase tracking-[0.3em] text-white/40">{label}</p>
    <p className="mt-2 text-lg font-semibold text-white">{value}</p>
  </div>
);

export default Projects;
